package com.caj_app.cuentas.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepositorio extends JpaRepository<ClienteEntidad, UUID> {
}
