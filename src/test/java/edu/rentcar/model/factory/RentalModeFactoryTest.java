package edu.rentcar.model.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalModeFactoryTest {

    @Test
    void basicFactoryCreatesMatchingInsuranceAndAssistance() {
        RentalModeFactory factory = new BasicModeFactory();

        assertInstanceOf(BasicInsurance.class, factory.createInsuranceCoverage());
        assertInstanceOf(BasicAssistance.class, factory.createRoadsideAssistance());
        assertEquals(12.50, factory.createInsuranceCoverage().getCoverageCost());
        assertFalse(factory.createRoadsideAssistance().IsFullService());
    }

    @Test
    void premiumFactoryCreatesMatchingInsuranceAndAssistance() {
        RentalModeFactory factory = new PremiumModeFactory();

        assertInstanceOf(PremiumInsurance.class, factory.createInsuranceCoverage());
        assertInstanceOf(PremiumAssistance.class, factory.createRoadsideAssistance());
        assertEquals(28.00, factory.createInsuranceCoverage().getCoverageCost());
    }

    @Test
    void allRiskFactoryCreatesFullCoverageAndAssistance() {
        RentalModeFactory factory = new AllRiskModeFactory();

        assertInstanceOf(AllRiskInsurance.class, factory.createInsuranceCoverage());
        assertInstanceOf(AllRiskAssistance.class, factory.createRoadsideAssistance());
        assertEquals(50.00, factory.createInsuranceCoverage().getCoverageCost());
        assertTrue(factory.createRoadsideAssistance().IsFullService());
    }
}
