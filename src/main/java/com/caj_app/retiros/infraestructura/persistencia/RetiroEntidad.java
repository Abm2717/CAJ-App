package com.caj_app.retiros.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.caj_app.retiros.dominio.EstadoRetiro;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(schema = "retiros", name = "retiro",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_retiro_clave", columnNames = "clave_idempotencia"),
				@UniqueConstraint(name = "uq_retiro_folio_conciliacion", columnNames = "folio_conciliacion") },
		indexes = @Index(name = "ix_retiro_estado_creado", columnList = "estado, creado_en"),
		check = @CheckConstraint(name = "ck_retiro_monto", constraint = "monto > 0"))
public class RetiroEntidad {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "clave_idempotencia", length = 64, nullable = false, updatable = false)
	private String claveIdempotencia;

	@Column(name = "cuenta_id", nullable = false, updatable = false)
	private UUID cuentaId;

	@Column(name = "cajero_id", nullable = false, updatable = false)
	private UUID cajeroId;

	@Column(name = "monto", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal monto;

	@Column(name = "comision_base", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal comisionBase;

	@Column(name = "tasa_iva", precision = 5, scale = 4, nullable = false, updatable = false)
	private BigDecimal tasaIva;

	@Column(name = "comision_total", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal comisionTotal;

	@Column(name = "monto_entregado", precision = 14, scale = 2)
	private BigDecimal montoEntregado;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "estado", length = 25, nullable = false)
	private EstadoRetiro estado;

	@Column(name = "folio_conciliacion", length = 20)
	private String folioConciliacion;

	@Column(name = "motivo", length = 60)
	private String motivo;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Column(name = "actualizado_en", nullable = false)
	private Instant actualizadoEn;

	protected RetiroEntidad() {
	}

	public RetiroEntidad(UUID id, String claveIdempotencia, UUID cuentaId, UUID cajeroId, BigDecimal monto,
			BigDecimal comisionBase, BigDecimal tasaIva, BigDecimal comisionTotal, EstadoRetiro estado,
			Instant creadoEn) {
		this.id = id;
		this.claveIdempotencia = claveIdempotencia;
		this.cuentaId = cuentaId;
		this.cajeroId = cajeroId;
		this.monto = monto;
		this.comisionBase = comisionBase;
		this.tasaIva = tasaIva;
		this.comisionTotal = comisionTotal;
		this.estado = estado;
		this.creadoEn = creadoEn;
		this.actualizadoEn = creadoEn;
	}

	public UUID getId() {
		return id;
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

	public BigDecimal getComisionBase() {
		return comisionBase;
	}

	public BigDecimal getTasaIva() {
		return tasaIva;
	}

	public BigDecimal getComisionTotal() {
		return comisionTotal;
	}

	public BigDecimal getMontoEntregado() {
		return montoEntregado;
	}

	public void setMontoEntregado(BigDecimal montoEntregado) {
		this.montoEntregado = montoEntregado;
	}

	public EstadoRetiro getEstado() {
		return estado;
	}

	public void setEstado(EstadoRetiro estado) {
		this.estado = estado;
	}

	public String getFolioConciliacion() {
		return folioConciliacion;
	}

	public void setFolioConciliacion(String folioConciliacion) {
		this.folioConciliacion = folioConciliacion;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public Instant getActualizadoEn() {
		return actualizadoEn;
	}

	public void setActualizadoEn(Instant actualizadoEn) {
		this.actualizadoEn = actualizadoEn;
	}

}
