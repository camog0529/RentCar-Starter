package edu.rentcar.model.factory;

/**
 * Fábrica Concreta para la Modalidad Premium.
 */
public class PremiumModeFactory implements RentalModeFactory {

    @Override
    public InsuranceCoverage createInsuranceCoverage() {
        return new PremiumInsurance();
    }

    @Override
    public RoadsideAssistance createRoadsideAssistance() {
        return new PremiumAssistance();
    }
}