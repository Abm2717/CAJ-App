package com.caj_app.cajero.efectivo.infraestructura;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CajeroJpaRepositorio extends JpaRepository<CajeroEntidad, UUID> {
}
