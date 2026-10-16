package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Cobertura de Seguro Premium.
 */
public class PremiumInsurance implements InsuranceCoverage {

    @Override
    public String getCoverageDetails() {
        return "Premium Insurance: Comprehensive collision, theft, and third-party protection with a low 10% deductible.";
    }

    @Override
    public double getCoverageCost() {
        return 28.00; // Costo diario intermedio
    }
}