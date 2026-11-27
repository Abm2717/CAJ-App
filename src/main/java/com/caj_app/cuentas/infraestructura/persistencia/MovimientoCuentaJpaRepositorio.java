package com.caj_app.cuentas.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCuentaJpaRepositorio extends JpaRepository<MovimientoCuentaEntidad, UUID> {
}
