package edu.rentcar.model.decorator;

/**
 * Interfaz componente que define el comportamiento de cualquier objeto rentable
 * en el sistema (ya sea un vehículo base o un servicio decorado).
 */
public interface Rentable {
    /** @return La descripción detallada del vehículo y sus servicios acumulados. */
    String getDescription();

    /** @return El costo diario total calculado de forma dinámica. */
    double getDailyRate();
}