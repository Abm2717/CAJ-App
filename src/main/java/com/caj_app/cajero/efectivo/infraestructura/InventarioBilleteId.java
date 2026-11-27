package com.caj_app.cajero.efectivo.infraestructura;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Llave compuesta de {@code cajero.inventario_billete}. */
@Embeddable
public record InventarioBilleteId(
		@Column(name = "cajero_id") UUID cajeroId,
		@Column(name = "denominacion") Integer denominacion) {
}
