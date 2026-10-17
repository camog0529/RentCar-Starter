package edu.rentcar.reservation;

import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Vehicle;
import edu.rentcar.model.composite.IndividualRentalService;
import edu.rentcar.model.composite.ServicePackage;
import edu.rentcar.model.factory.BasicModeFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalCostCalculatorTest {
    private final RentalCostCalculator calculator = new RentalCostCalculator();

    @Test
    void calculatesVehicleAndCoverageForEveryRentalDay() {
        RentalRequest request = request();

        assertEquals(new BigDecimal("225.00"), calculator.calculate(request));
    }

    @Test
    void includesCostOfNestedCompositeServicesForEveryRentalDay() {
        ServicePackage bundle = new ServicePackage("Paquete");
        bundle.add(new IndividualRentalService("GPS", new BigDecimal("5.00")));
        ServicePackage nested = new ServicePackage("Confort");
        nested.add(new IndividualRentalService("Silla", new BigDecimal("7.50")));
        bundle.add(nested);
        RentalRequest request = requestBuilder().addService(bundle).build();

        assertEquals(new BigDecimal("250.00"), calculator.calculate(request));
    }

    @Test
    void rejectsNullRequestAndInvalidDateRange() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
        RentalRequest request = requestBuilder()
                .dates(LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23))
                .build();
        assertEquals(new BigDecimal("112.50"), calculator.calculate(request));
    }

    private RentalRequest request() {
        return requestBuilder().build();
    }

    private RentalRequest.Builder requestBuilder() {
        return new RentalRequest.Builder()
                .customer(new Customer("CUS-1", "Ana", "ana@example.com", true))
                .vehicle(new Vehicle("CAR-1", "Toyota", "Corolla", "Economy", 2024, 100, "Available"))
                .dates(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22))
                .rentalModeFactory(new BasicModeFactory());
    }
}
