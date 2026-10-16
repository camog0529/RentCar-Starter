package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Cobertura de Seguro Todo Riesgo.
 */
public class AllRiskInsurance implements InsuranceCoverage {

    @Override
    public String getCoverageDetails() {
        return "All Risk Insurance: Total protection against all collision damages, natural disasters, and theft. Zero deductible.";
    }

    @Override
    public double getCoverageCost() {
        return 50.00; // Costo de protección total
    }
}