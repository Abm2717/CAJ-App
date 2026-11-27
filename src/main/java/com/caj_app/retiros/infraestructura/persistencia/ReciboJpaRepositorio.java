package com.caj_app.retiros.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReciboJpaRepositorio extends JpaRepository<ReciboEntidad, UUID> {
}
