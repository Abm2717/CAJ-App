package com.caj_app.retiros.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import com.caj_app.retiros.dominio.EstadoClave;

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
@Table(schema = "retiros", name = "clave_idempotencia")
public class ClaveIdempotenciaEntidad implements Persistable<String> {

	@Id
	@Column(name = "clave", length = 64)
	private String clave;

	@Column(name = "cuenta_id", nullable = false, updatable = false)
	private UUID cuentaId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "hash_solicitud", length = 64, nullable = false, updatable = false)
	private String hashSolicitud;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "estado", length = 15, nullable = false)
	private EstadoClave estado;

	@Column(name = "codigo_http")
	private Integer codigoHttp;

	@Column(name = "respuesta", length = 4000)
	private String respuesta;

	@Column(name = "creada_en", nullable = false, updatable = false)
	private Instant creadaEn;

	@Column(name = "expira_en", nullable = false, updatable = false)
	private Instant expiraEn;

	@Transient
	private boolean nueva = true;

	protected ClaveIdempotenciaEntidad() {
	}

	public ClaveIdempotenciaEntidad(String clave, UUID cuentaId, String hashSolicitud, EstadoClave estado,
			Instant creadaEn, Instant expiraEn) {
		this.clave = clave;
		this.cuentaId = cuentaId;
		this.hashSolicitud = hashSolicitud;
		this.estado = estado;
		this.creadaEn = creadaEn;
		this.expiraEn = expiraEn;
	}

	@Override
	public String getId() {
		return clave;
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

	public String getClave() {
		return clave;
	}

	public UUID getCuentaId() {
		return cuentaId;
	}

	public String getHashSolicitud() {
		return hashSolicitud;
	}

	public EstadoClave getEstado() {
		return estado;
	}

	public void setEstado(EstadoClave estado) {
		this.estado = estado;
	}

	public Integer getCodigoHttp() {
		return codigoHttp;
	}

	public void setCodigoHttp(Integer codigoHttp) {
		this.codigoHttp = codigoHttp;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public Instant getCreadaEn() {
		return creadaEn;
	}

	public Instant getExpiraEn() {
		return expiraEn;
	}

}
