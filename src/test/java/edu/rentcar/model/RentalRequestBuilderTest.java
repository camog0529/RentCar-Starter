package edu.rentcar.model;

import edu.rentcar.model.composite.IndividualRentalService;
import edu.rentcar.model.factory.BasicModeFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalRequestBuilderTest {

    @Test
    void buildsRequestWithRequiredDataAndOptionalService() {
        Customer customer = new Customer("CUS-1", "Ana Pérez", "ana@example.com", true);
        Vehicle vehicle = new Vehicle("CAR-1", "Toyota", "Corolla", "Economy", 2024, 100, "Available");
        IndividualRentalService gps = new IndividualRentalService("GPS", new BigDecimal("5.00"));

        RentalRequest request = new RentalRequest.Builder()
                .customer(customer)
                .vehicle(vehicle)
                .dates(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22))
                .rentalModeFactory(new BasicModeFactory())
                .addService(gps)
                .build();

        assertSame(customer, request.getCustomer());
        assertSame(vehicle, request.getVehicle());
        assertEquals(LocalDate.of(2026, 10, 20), request.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 22), request.getEndDate());
        assertInstanceOf(BasicModeFactory.class, request.getRentalModeFactory());
        assertEquals(java.util.List.of(gps), request.getServices());
    }

    @Test
    void refusesToBuildWhenRequiredInformationIsMissing() {
        assertThrows(IllegalStateException.class, () -> new RentalRequest.Builder().build());
    }

    @Test
    void refusesDatesWhereStartIsNotBeforeEnd() {
        assertThrows(IllegalArgumentException.class,
                () -> new RentalRequest.Builder().dates(
                        LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 22)));
    }

    @Test
    void builtRequestKeepsItsServiceListIndependentAndReadOnly() {
        RentalRequest.Builder builder = completeBuilder();
        IndividualRentalService gps = new IndividualRentalService("GPS", new BigDecimal("5.00"));
        builder.addService(gps);
        RentalRequest request = builder.build();
        builder.addService(new IndividualRentalService("Silla", new BigDecimal("7.50")));

        assertEquals(1, request.getServices().size());
        assertThrows(UnsupportedOperationException.class,
                () -> request.getServices().add(new IndividualRentalService("Extra", BigDecimal.ONE)));
    }

    private RentalRequest.Builder completeBuilder() {
        return new RentalRequest.Builder()
                .customer(new Customer("CUS-1", "Ana Pérez", "ana@example.com", true))
                .vehicle(new Vehicle("CAR-1", "Toyota", "Corolla", "Economy", 2024, 100, "Available"))
                .dates(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22))
                .rentalModeFactory(new BasicModeFactory());
    }
}
