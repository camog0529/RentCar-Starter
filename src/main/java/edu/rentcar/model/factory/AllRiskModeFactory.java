package edu.rentcar.model.factory;

/**
 * Fábrica Concreta para la Modalidad Todo Riesgo.
 */
public class AllRiskModeFactory implements RentalModeFactory {

    @Override
    public InsuranceCoverage createInsuranceCoverage() {
        return new AllRiskInsurance();
    }

    @Override
    public RoadsideAssistance createRoadsideAssistance() {
        return new AllRiskAssistance();
    }
}