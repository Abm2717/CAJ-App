package com.caj_app.cajero.efectivo.infraestructura;

import java.time.Instant;
import java.util.UUID;

import com.caj_app.cajero.efectivo.dominio.EstadoReserva;

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
@Table(schema = "cajero", name = "reserva_efectivo")
public class ReservaEfectivoEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "operacion_id")
	private UUID operacionId;

	@Column(name = "cajero_id", nullable = false, updatable = false)
	private UUID cajeroId;

	@Column(name = "billetes_200", nullable = false, updatable = false)
	private int billetes200;

	@Column(name = "billetes_500", nullable = false, updatable = false)
	private int billetes500;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "estado", length = 15, nullable = false)
	private EstadoReserva estado;

	@Column(name = "creada_en", nullable = false, updatable = false)
	private Instant creadaEn;

	@Column(name = "actualizada_en", nullable = false)
	private Instant actualizadaEn;

	@Transient
	private boolean nueva = true;

	protected ReservaEfectivoEntidad() {
	}

	public ReservaEfectivoEntidad(UUID operacionId, UUID cajeroId, int billetes200, int billetes500,
			EstadoReserva estado, Instant creadaEn) {
		this.operacionId = operacionId;
		this.cajeroId = cajeroId;
		this.billetes200 = billetes200;
		this.billetes500 = billetes500;
		this.estado = estado;
		this.creadaEn = creadaEn;
		this.actualizadaEn = creadaEn;
	}

	@Override
	public UUID getId() {
		return operacionId;
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

	public UUID getOperacionId() {
		return operacionId;
	}

	public UUID getCajeroId() {
		return cajeroId;
	}

	public int getBilletes200() {
		return billetes200;
	}

	public int getBilletes500() {
		return billetes500;
	}

	public EstadoReserva getEstado() {
		return estado;
	}

	public void setEstado(EstadoReserva estado) {
		this.estado = estado;
	}

	public Instant getCreadaEn() {
		return creadaEn;
	}

	public Instant getActualizadaEn() {
		return actualizadaEn;
	}

	public void setActualizadaEn(Instant actualizadaEn) {
		this.actualizadaEn = actualizadaEn;
	}

}
