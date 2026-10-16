package edu.rentcar.model.factory;

/**
 * Interfaz base que define el comportamiento para las Coberturas de Seguro.
 */
public interface InsuranceCoverage {
    String getCoverageDetails();
    double getCoverageCost();
}