package com.caj_app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.caj_app.cajero.api.Resultado;
import com.caj_app.cajero.api.TipoEvento;
import com.caj_app.cajero.auditoria.infraestructura.BitacoraSoloInsercion;
import com.caj_app.cajero.auditoria.infraestructura.RegistroBitacoraEntidad;
import com.caj_app.cajero.efectivo.dominio.EstadoReserva;
import com.caj_app.cajero.efectivo.infraestructura.CajeroEntidad;
import com.caj_app.cajero.efectivo.infraestructura.CajeroJpaRepositorio;
import com.caj_app.cajero.efectivo.infraestructura.InventarioBilleteEntidad;
import com.caj_app.cajero.efectivo.infraestructura.InventarioBilleteId;
import com.caj_app.cajero.efectivo.infraestructura.InventarioBilleteJpaRepositorio;
import com.caj_app.cajero.efectivo.infraestructura.ReservaEfectivoEntidad;
import com.caj_app.cajero.efectivo.infraestructura.ReservaEfectivoJpaRepositorio;
import com.caj_app.cuentas.dominio.EstadoCuenta;
import com.caj_app.cuentas.dominio.TipoMovimiento;
import com.caj_app.cuentas.infraestructura.persistencia.ClienteEntidad;
import com.caj_app.cuentas.infraestructura.persistencia.ClienteJpaRepositorio;
import com.caj_app.cuentas.infraestructura.persistencia.CuentaEntidad;
import com.caj_app.cuentas.infraestructura.persistencia.CuentaJpaRepositorio;
import com.caj_app.cuentas.infraestructura.persistencia.MovimientoCuentaEntidad;
import com.caj_app.cuentas.infraestructura.persistencia.MovimientoCuentaJpaRepositorio;
import com.caj_app.retiros.dominio.EstadoClave;
import com.caj_app.retiros.dominio.EstadoRetiro;
import com.caj_app.retiros.dominio.ResultadoIntento;
import com.caj_app.retiros.infraestructura.persistencia.ClaveIdempotenciaEntidad;
import com.caj_app.retiros.infraestructura.persistencia.ClaveIdempotenciaJpaRepositorio;
import com.caj_app.retiros.infraestructura.persistencia.IntentoDispensacionEntidad;
import com.caj_app.retiros.infraestructura.persistencia.IntentoDispensacionJpaRepositorio;
import com.caj_app.retiros.infraestructura.persistencia.ReciboEntidad;
import com.caj_app.retiros.infraestructura.persistencia.ReciboJpaRepositorio;
import com.caj_app.retiros.infraestructura.persistencia.RetiroEntidad;
import com.caj_app.retiros.infraestructura.persistencia.RetiroJpaRepositorio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PersistenciaTest {

	private static final Instant AHORA = Instant.parse("2026-01-15T18:00:00Z");

	@Autowired JdbcTemplate jdbc;
	@Autowired ClienteJpaRepositorio clientes;
	@Autowired CuentaJpaRepositorio cuentas;
	@Autowired MovimientoCuentaJpaRepositorio movimientos;
	@Autowired ClaveIdempotenciaJpaRepositorio claves;
	@Autowired RetiroJpaRepositorio retiros;
	@Autowired IntentoDispensacionJpaRepositorio intentos;
	@Autowired ReciboJpaRepositorio recibos;
	@Autowired CajeroJpaRepositorio cajeros;
	@Autowired InventarioBilleteJpaRepositorio inventario;
	@Autowired ReservaEfectivoJpaRepositorio reservas;
	@Autowired BitacoraSoloInsercion bitacora;

	@Test
	void creaLasTablasDelPlanEnElEsquemaDeSuModulo() {
		var tablas = jdbc.queryForList("""
				select table_schema || '.' || table_name from information_schema.tables
				where table_schema in ('cuentas', 'retiros', 'cajero')""", String.class);

		assertThat(tablas).containsExactlyInAnyOrder(
				"cuentas.cliente", "cuentas.cuenta", "cuentas.movimiento_cuenta",
				"retiros.clave_idempotencia", "retiros.retiro", "retiros.intento_dispensacion", "retiros.recibo",
				"cajero.cajero", "cajero.inventario_billete", "cajero.reserva_efectivo", "cajero.bitacora");
	}

	@Test
	void guardaYLeeTodasLasEntidades() {
		var cliente = clientes.save(new ClienteEntidad(UUID.randomUUID(), "Persona de Prueba", AHORA));
		var cuenta = cuentas.save(cuenta(cliente.getId(), "6000.00"));
		var movimiento = movimientos.save(movimiento(cuenta.getId(), UUID.randomUUID()));
		var cajero = cajeros.save(new CajeroEntidad(UUID.randomUUID(), "CAJ-001", "Sucursal", "ACTIVO"));
		var billetes = inventario.save(
				new InventarioBilleteEntidad(new InventarioBilleteId(cajero.getId(), 200), 10, 0));
		var clave = claves.save(clave(UUID.randomUUID().toString(), cuenta.getId()));
		var retiro = retiros.save(new RetiroEntidad(UUID.randomUUID(), clave.getClave(), cuenta.getId(),
				cajero.getId(), new BigDecimal("200.00"), new BigDecimal("30.00"), new BigDecimal("0.1600"),
				new BigDecimal("34.80"), EstadoRetiro.CARGADO, AHORA));
		var intento = intentos.save(new IntentoDispensacionEntidad(UUID.randomUUID(), retiro.getId(),
				ResultadoIntento.EXITO, new BigDecimal("200.00"), new BigDecimal("200.00"), AHORA, AHORA));
		recibos.save(new ReciboEntidad(retiro.getId(), "R-0001", "****1234", "Persona de Prueba",
				new BigDecimal("200.00"), new BigDecimal("34.80"), AHORA));
		reservas.save(new ReservaEfectivoEntidad(retiro.getId(), cajero.getId(), 1, 0, EstadoReserva.RESERVADA, AHORA));
		var registro = bitacora.save(new RegistroBitacoraEntidad(AHORA, TipoEvento.CARGO_APLICADO, Resultado.EN_CURSO,
				retiro.getId(), clave.getClave(), cuenta.getId(), cajero.getId(), new BigDecimal("200.00"),
				new BigDecimal("34.80"), null, null, null));

		assertThat(clientes.findById(cliente.getId())).isPresent();
		assertThat(cuentas.findById(cuenta.getId()).orElseThrow().getClienteId()).isEqualTo(cliente.getId());
		assertThat(movimientos.findById(movimiento.getId())).isPresent();
		assertThat(cajeros.findById(cajero.getId())).isPresent();
		assertThat(inventario.findById(billetes.getId()).orElseThrow().getDisponibles()).isEqualTo(10);
		assertThat(retiros.findById(retiro.getId()).orElseThrow().getTasaIva()).isEqualByComparingTo("0.16");
		assertThat(intentos.findById(intento.getId())).isPresent();
		assertThat(recibos.findById(retiro.getId())).isPresent();
		assertThat(reservas.findById(retiro.getId()).orElseThrow().getBilletes200()).isEqualTo(1);
		assertThat(bitacora.findByOperacionIdOrderById(retiro.getId()))
				.extracting(RegistroBitacoraEntidad::getId).containsExactly(registro.getId());
	}

	@Test
	void unaClaveDeIdempotenciaRepetidaNoSobrescribeLaOriginal() {
		var valor = UUID.randomUUID().toString();
		var cuentaOriginal = UUID.randomUUID();
		claves.saveAndFlush(clave(valor, cuentaOriginal));

		assertThatThrownBy(() -> claves.saveAndFlush(clave(valor, UUID.randomUUID())))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(claves.findById(valor).orElseThrow().getCuentaId()).isEqualTo(cuentaOriginal);
	}

	@Test
	void noAdmiteDosMovimientosDelMismoTipoParaUnaOperacion() {
		var cuenta = cuentaGuardada("100.00");
		var operacion = UUID.randomUUID();
		movimientos.saveAndFlush(movimiento(cuenta.getId(), operacion));

		assertThatThrownBy(() -> movimientos.saveAndFlush(movimiento(cuenta.getId(), operacion)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void noAdmiteSaldoNegativo() {
		assertThatThrownBy(() -> cuentaGuardada("-0.01"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private CuentaEntidad cuentaGuardada(String saldo) {
		var cliente = clientes.save(new ClienteEntidad(UUID.randomUUID(), "Persona de Prueba", AHORA));
		return cuentas.saveAndFlush(cuenta(cliente.getId(), saldo));
	}

	private static CuentaEntidad cuenta(UUID clienteId, String saldo) {
		var numero = UUID.randomUUID().toString().substring(0, 20);
		return new CuentaEntidad(UUID.randomUUID(), clienteId, numero, new BigDecimal(saldo), "MXN",
				EstadoCuenta.ACTIVA, AHORA);
	}

	private static MovimientoCuentaEntidad movimiento(UUID cuentaId, UUID operacionId) {
		return new MovimientoCuentaEntidad(UUID.randomUUID(), cuentaId, operacionId, TipoMovimiento.CARGO_RETIRO,
				new BigDecimal("50.00"), new BigDecimal("50.00"), LocalDate.of(2026, 1, 15), AHORA);
	}

	private static ClaveIdempotenciaEntidad clave(String clave, UUID cuentaId) {
		return new ClaveIdempotenciaEntidad(clave, cuentaId, "a".repeat(64), EstadoClave.EN_PROCESO, AHORA,
				AHORA.plusSeconds(86_400));
	}

}
