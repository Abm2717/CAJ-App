package com.caj_app.retiros.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.caj_app.retiros.dominio.ResultadoIntento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

@Entity
@Table(schema = "retiros", name = "intento_dispensacion")
public class IntentoDispensacionEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "retiro_id", nullable = false, updatable = false)
	private UUID retiroId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "resultado", length = 15, nullable = false, updatable = false)
	private ResultadoIntento resultado;

	@Column(name = "monto_solicitado", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal montoSolicitado;

	/** Nulo cuando no se sabe cuánto salió (sin respuesta). */
	@Column(name = "monto_entregado", precision = 14, scale = 2, updatable = false)
	private BigDecimal montoEntregado;

	@Column(name = "iniciado_en", nullable = false, updatable = false)
	private Instant iniciadoEn;

	@Column(name = "terminado_en", updatable = false)
	private Instant terminadoEn;

	@Transient
	private boolean nueva = true;

	protected IntentoDispensacionEntidad() {
	}

	public IntentoDispensacionEntidad(UUID id, UUID retiroId, ResultadoIntento resultado, BigDecimal montoSolicitado,
			BigDecimal montoEntregado, Instant iniciadoEn, Instant terminadoEn) {
		this.id = id;
		this.retiroId = retiroId;
		this.resultado = resultado;
		this.montoSolicitado = montoSolicitado;
		this.montoEntregado = montoEntregado;
		this.iniciadoEn = iniciadoEn;
		this.terminadoEn = terminadoEn;
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

	public UUID getRetiroId() {
		return retiroId;
	}

	public ResultadoIntento getResultado() {
		return resultado;
	}

	public BigDecimal getMontoSolicitado() {
		return montoSolicitado;
	}

	public BigDecimal getMontoEntregado() {
		return montoEntregado;
	}

	public Instant getIniciadoEn() {
		return iniciadoEn;
	}

	public Instant getTerminadoEn() {
		return terminadoEn;
	}

}
