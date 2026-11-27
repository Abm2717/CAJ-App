package com.caj_app.cajero.auditoria.infraestructura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

/** Solo inserta y consulta: extiende {@link Repository} para no exponer borrado. */
public interface BitacoraSoloInsercion extends Repository<RegistroBitacoraEntidad, Long> {

	RegistroBitacoraEntidad save(RegistroBitacoraEntidad registro);

	Optional<RegistroBitacoraEntidad> findById(Long id);

	List<RegistroBitacoraEntidad> findByOperacionIdOrderById(UUID operacionId);

}
