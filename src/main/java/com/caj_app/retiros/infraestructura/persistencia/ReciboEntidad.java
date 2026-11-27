package com.caj_app.retiros.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import org.springframework.data.domain.Persistable;

@Entity
@Table(schema = "retiros", name = "recibo",
		uniqueConstraints = @UniqueConstraint(name = "uq_recibo_folio", columnNames = "folio"))
public class ReciboEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "retiro_id")
	private UUID retiroId;

	@Column(name = "folio", length = 20, nullable = false, updatable = false)
	private String folio;

	@Column(name = "cuenta_enmascarada", length = 8, nullable = false, updatable = false)
	private String cuentaEnmascarada;

	@Column(name = "nombre_titular", length = 150, nullable = false, updatable = false)
	private String nombreTitular;

	@Column(name = "monto_retirado", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal montoRetirado;

	@Column(name = "comision_cobrada", precision = 14, scale = 2, nullable = false, updatable = false)
	private BigDecimal comisionCobrada;

	@Column(name = "emitido_en", nullable = false, updatable = false)
	private Instant emitidoEn;

	@Transient
	private boolean nueva = true;

	protected ReciboEntidad() {
	}

	public ReciboEntidad(UUID retiroId, String folio, String cuentaEnmascarada, String nombreTitular,
			BigDecimal montoRetirado, BigDecimal comisionCobrada, Instant emitidoEn) {
		this.retiroId = retiroId;
		this.folio = folio;
		this.cuentaEnmascarada = cuentaEnmascarada;
		this.nombreTitular = nombreTitular;
		this.montoRetirado = montoRetirado;
		this.comisionCobrada = comisionCobrada;
		this.emitidoEn = emitidoEn;
	}

	@Override
	public UUID getId() {
		return retiroId;
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

	public String getFolio() {
		return folio;
	}

	public String getCuentaEnmascarada() {
		return cuentaEnmascarada;
	}

	public String getNombreTitular() {
		return nombreTitular;
	}

	public BigDecimal getMontoRetirado() {
		return montoRetirado;
	}

	public BigDecimal getComisionCobrada() {
		return comisionCobrada;
	}

	public Instant getEmitidoEn() {
		return emitidoEn;
	}

}
