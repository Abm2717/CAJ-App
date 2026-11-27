package com.caj_app.cuentas.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

@Entity
@Table(schema = "cuentas", name = "cliente")
public class ClienteEntidad implements Persistable<UUID> {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "nombre_completo", length = 150, nullable = false)
	private String nombreCompleto;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Transient
	private boolean nueva = true;

	protected ClienteEntidad() {
	}

	public ClienteEntidad(UUID id, String nombreCompleto, Instant creadoEn) {
		this.id = id;
		this.nombreCompleto = nombreCompleto;
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

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

}
