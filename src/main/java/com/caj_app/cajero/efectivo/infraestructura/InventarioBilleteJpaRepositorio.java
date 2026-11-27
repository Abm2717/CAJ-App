package com.caj_app.cajero.efectivo.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioBilleteJpaRepositorio
		extends JpaRepository<InventarioBilleteEntidad, InventarioBilleteId> {
}
