package com.caj_app.cuentas.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.caj_app.cuentas.dominio.EstadoCuenta;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(schema = "cuentas", name = "cuenta",
		uniqueConstraints = @UniqueConstraint(name = "uq_cuenta_numero", columnNames = "numero"),
		check = @CheckConstraint(name = "ck_cuenta_saldo", constraint = "saldo >= 0"))
public class CuentaEntidad {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "cliente_id", nullable = false, updatable = false)
	private UUID clienteId;

	@Column(name = "numero", length = 20, nullable = false, updatable = false)
	private String numero;

	@Column(name = "saldo", precision = 14, scale = 2, nullable = false)
	private BigDecimal saldo;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "moneda", length = 3, nullable = false, updatable = false)
	private String moneda;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "estado", length = 15, nullable = false)
	private EstadoCuenta estado;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "actualizado_en", nullable = false)
	private Instant actualizadoEn;

	protected CuentaEntidad() {
	}

	public CuentaEntidad(UUID id, UUID clienteId, String numero, BigDecimal saldo, String moneda,
			EstadoCuenta estado, Instant actualizadoEn) {
		this.id = id;
		this.clienteId = clienteId;
		this.numero = numero;
		this.saldo = saldo;
		this.moneda = moneda;
		this.estado = estado;
		this.actualizadoEn = actualizadoEn;
	}

	public UUID getId() {
		return id;
	}

	public UUID getClienteId() {
		return clienteId;
	}

	public String getNumero() {
		return numero;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public String getMoneda() {
		return moneda;
	}

	public EstadoCuenta getEstado() {
		return estado;
	}

	public void setEstado(EstadoCuenta estado) {
		this.estado = estado;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getActualizadoEn() {
		return actualizadoEn;
	}

	public void setActualizadoEn(Instant actualizadoEn) {
		this.actualizadoEn = actualizadoEn;
	}

}
