package com.caj_app.cajero.efectivo.infraestructura;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaEfectivoJpaRepositorio extends JpaRepository<ReservaEfectivoEntidad, UUID> {
}
