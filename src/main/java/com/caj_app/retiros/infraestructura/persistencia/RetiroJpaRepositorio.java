package com.caj_app.retiros.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RetiroJpaRepositorio extends JpaRepository<RetiroEntidad, UUID> {
}
