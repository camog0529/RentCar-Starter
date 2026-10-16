package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Cobertura de Seguro Básica.
 * Cumple con el requerimiento de negocio de proveer el seguro mínimo obligatorio.
 */
public class BasicInsurance implements InsuranceCoverage {

    /**
     * @return Descripción técnica detallada del seguro básico.
     */
    @Override
    public String getCoverageDetails() {
        return "Basic Insurance: Mandatory third-party liability coverage only. High deductible applies.";
    }

    /**
     * @return El costo diario fijo asignado a esta cobertura de seguro.
     */
    @Override
    public double getCoverageCost() {
        return 12.50; // Tarifa diaria base expresada en dólares/moneda local
    }
}