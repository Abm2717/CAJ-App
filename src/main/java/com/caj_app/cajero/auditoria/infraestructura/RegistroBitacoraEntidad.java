package com.caj_app.cajero.auditoria.infraestructura;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.caj_app.cajero.api.Resultado;
import com.caj_app.cajero.api.TipoEvento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(schema = "cajero", name = "bitacora",
		indexes = {
				@Index(name = "ix_bitacora_operacion", columnList = "operacion_id"),
				@Index(name = "ix_bitacora_cuenta_fecha", columnList = "cuenta_id, ocurrido_en") })
public class RegistroBitacoraEntidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false)
	private Long id;

	@Column(name = "ocurrido_en", nullable = false, updatable = false)
	private Instant ocurridoEn;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "tipo_evento", length = 30, nullable = false, updatable = false)
	private TipoEvento tipoEvento;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "resultado", length = 25, nullable = false, updatable = false)
	private Resultado resultado;

	@Column(name = "operacion_id", updatable = false)
	private UUID operacionId;

	@Column(name = "clave_idempotencia", length = 64, updatable = false)
	private String claveIdempotencia;

	@Column(name = "cuenta_id", updatable = false)
	private UUID cuentaId;

	@Column(name = "cajero_id", updatable = false)
	private UUID cajeroId;

	@Column(name = "monto", precision = 14, scale = 2, updatable = false)
	private BigDecimal monto;

	@Column(name = "comision", precision = 14, scale = 2, updatable = false)
	private BigDecimal comision;

	@Column(name = "motivo", length = 60, updatable = false)
	private String motivo;

	@Column(name = "detalle", length = 4000, updatable = false)
	private String detalle;

	@Column(name = "correlacion_id", length = 64, updatable = false)
	private String correlacionId;

	protected RegistroBitacoraEntidad() {
	}

	public RegistroBitacoraEntidad(Instant ocurridoEn, TipoEvento tipoEvento, Resultado resultado, UUID operacionId,
			String claveIdempotencia, UUID cuentaId, UUID cajeroId, BigDecimal monto, BigDecimal comision,
			String motivo, String detalle, String correlacionId) {
		this.ocurridoEn = ocurridoEn;
		this.tipoEvento = tipoEvento;
		this.resultado = resultado;
		this.operacionId = operacionId;
		this.claveIdempotencia = claveIdempotencia;
		this.cuentaId = cuentaId;
		this.cajeroId = cajeroId;
		this.monto = monto;
		this.comision = comision;
		this.motivo = motivo;
		this.detalle = detalle;
		this.correlacionId = correlacionId;
	}

	public Long getId() {
		return id;
	}

	public Instant getOcurridoEn() {
		return ocurridoEn;
	}

	public TipoEvento getTipoEvento() {
		return tipoEvento;
	}

	public Resultado getResultado() {
		return resultado;
	}

	public UUID getOperacionId() {
		return operacionId;
	}

	public String getClaveIdempotencia() {
		return claveIdempotencia;
	}

	public UUID getCuentaId() {
		return cuentaId;
	}

	public UUID getCajeroId() {
		return cajeroId;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public BigDecimal getComision() {
		return comision;
	}

	public String getMotivo() {
		return motivo;
	}

	public String getDetalle() {
		return detalle;
	}

	public String getCorrelacionId() {
		return correlacionId;
	}

}
