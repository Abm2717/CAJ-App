package com.caj_app.cajero.efectivo.infraestructura;

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
@Table(schema = "cajero", name = "cajero",
		uniqueConstraints = @UniqueConstraint(name = "uq_cajero_codigo", columnNames = "codigo"))
public class CajeroEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "codigo", length = 20, nullable = false, updatable = false)
	private String codigo;

	@Column(name = "ubicacion", length = 150, nullable = false)
	private String ubicacion;

	/** El plan no define los valores permitidos, por eso es texto libre. */
	@Column(name = "estado", length = 15, nullable = false)
	private String estado;

	@Transient
	private boolean nueva = true;

	protected CajeroEntidad() {
	}

	public CajeroEntidad(UUID id, String codigo, String ubicacion, String estado) {
		this.id = id;
		this.codigo = codigo;
		this.ubicacion = ubicacion;
		this.estado = estado;
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

	public String getCodigo() {
		return codigo;
	}

	public String getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(String ubicacion) {
		this.ubicacion = ubicacion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

}
