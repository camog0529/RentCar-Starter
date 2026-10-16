package edu.rentcar.model.factory;

/**
 * Fábrica Abstracta que define los métodos de creación para las familias de productos.
 */
public interface RentalModeFactory {
    InsuranceCoverage createInsuranceCoverage();
    RoadsideAssistance createRoadsideAssistance();
}