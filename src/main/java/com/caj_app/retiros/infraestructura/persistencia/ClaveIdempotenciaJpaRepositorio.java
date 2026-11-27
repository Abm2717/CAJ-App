package com.caj_app.retiros.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveIdempotenciaJpaRepositorio extends JpaRepository<ClaveIdempotenciaEntidad, String> {
}
