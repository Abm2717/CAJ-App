package com.caj_app.cuentas.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.caj_app.cuentas.dominio.TipoMovimiento;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

@Entity
@Table(schema = "cuentas", name = "movimiento_cuenta",
		uniqueConstraints = @UniqueConstraint(name = "uq_movimiento_operacion_tipo",
				columnNames = { "operacion_id", "tipo" }),
		indexes = @Index(name = "ix_movimiento_cuenta_fecha", columnList = "cuenta_id, fecha_contable"),
		check = @CheckConstraint(name = "ck_movimiento_monto", constraint = "monto > 0"))
public class MovimientoCuentaEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "cuenta_id", nullable = false, updatable = false)
	private UUID cuentaId;

	@Column(name = "operacion_id", nullable = false, updatable = false)
	private UUID operacionId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "tipo", length = 30, nullable = false, updatable = false)
	private TipoMovimiento tipo;

	@Column(name = "monto", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal monto;

	@Column(name = "saldo_resultante", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal saldoResultante;

	@Column(name = "fecha_contable", nullable = false, updatable = false)
	private LocalDate fechaContable;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Transient
	private boolean nueva = true;

	protected MovimientoCuentaEntidad() {
	}

	public MovimientoCuentaEntidad(UUID id, UUID cuentaId, UUID operacionId, TipoMovimiento tipo, BigDecimal monto,
			BigDecimal saldoResultante, LocalDate fechaContable, Instant creadoEn) {
		this.id = id;
		this.cuentaId = cuentaId;
		this.operacionId = operacionId;
		this.tipo = tipo;
		this.monto = monto;
		this.saldoResultante = saldoResultante;
		this.fechaContable = fechaContable;
		this.creadoEn = creadoEn;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return nueva;
	}

	@PostLoad
	@PostPersist
	void marcarComoGuardada() {
		nueva = false;
	}

	public UUID getCuentaId() {
		return cuentaId;
	}

	public UUID getOperacionId() {
		return operacionId;
	}

	public TipoMovimiento getTipo() {
		return tipo;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public BigDecimal getSaldoResultante() {
		return saldoResultante;
	}

	public LocalDate getFechaContable() {
		return fechaContable;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

}
