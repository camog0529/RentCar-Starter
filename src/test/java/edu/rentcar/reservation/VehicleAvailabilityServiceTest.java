package edu.rentcar.reservation;

import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.Vehicle;
import edu.rentcar.model.factory.BasicModeFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VehicleAvailabilityServiceTest {

    @Test
    void availableVehicleWithNoConflictingReservationCanBeBooked() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        VehicleAvailabilityService service = new VehicleAvailabilityService(repository);

        assertTrue(service.isAvailable(request("Available", LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22))));
    }

    @Test
    void unavailableVehicleCannotBeBooked() {
        VehicleAvailabilityService service = new VehicleAvailabilityService(new InMemoryReservationRepository());

        assertFalse(service.isAvailable(request("Rented", LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22))));
    }

    @Test
    void adjacentConfirmedReservationDoesNotConflict() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        repository.save(new Reservation("RES-OLD", "CUS-OLD", "CAR-1",
                LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 20), "CONFIRMED"));
        VehicleAvailabilityService service = new VehicleAvailabilityService(repository);

        assertTrue(service.isAvailable(request("Available", LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22))));
    }

    @Test
    void overlappingConfirmedReservationForSameVehicleConflicts() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        repository.save(new Reservation("RES-OLD", "CUS-OLD", "CAR-1",
                LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 21), "CONFIRMED"));
        VehicleAvailabilityService service = new VehicleAvailabilityService(repository);

        assertFalse(service.isAvailable(request("Available", LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22))));
    }

    private RentalRequest request(String status, LocalDate start, LocalDate end) {
        return new RentalRequest.Builder()
                .customer(new Customer("CUS-1", "Ana", "ana@example.com", true))
                .vehicle(new Vehicle("CAR-1", "Toyota", "Corolla", "Economy", 2024, 100, status))
                .dates(start, end)
                .rentalModeFactory(new BasicModeFactory())
                .build();
    }
}
