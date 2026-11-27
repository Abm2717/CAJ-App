package com.caj_app.cajero.efectivo.infraestructura;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(schema = "cajero", name = "inventario_billete",
		check = {
				@CheckConstraint(name = "ck_inventario_denominacion", constraint = "denominacion in (200, 500)"),
				@CheckConstraint(name = "ck_inventario_existencias",
						constraint = "disponibles >= 0 and reservados >= 0") })
public class InventarioBilleteEntidad {

	@EmbeddedId
	private InventarioBilleteId id;

	@Column(name = "disponibles", nullable = false)
	private int disponibles;

	@Column(name = "reservados", nullable = false)
	private int reservados;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	protected InventarioBilleteEntidad() {
	}

	public InventarioBilleteEntidad(InventarioBilleteId id, int disponibles, int reservados) {
		this.id = id;
		this.disponibles = disponibles;
		this.reservados = reservados;
	}

	public InventarioBilleteId getId() {
		return id;
	}

	public int getDisponibles() {
		return disponibles;
	}

	public void setDisponibles(int disponibles) {
		this.disponibles = disponibles;
	}

	public int getReservados() {
		return reservados;
	}

	public void setReservados(int reservados) {
		this.reservados = reservados;
	}

	public Long getVersion() {
		return version;
	}

}
