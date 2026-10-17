package edu.rentcar.reservation;

import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.Vehicle;
import edu.rentcar.model.factory.BasicModeFactory;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ReservationWorkflowFacadeTest {

    @Test
    void approvedPaymentCreatesConfirmedReservationAndSendsCalculatedAmount() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicReference<PaymentRequest> capturedPayment = new AtomicReference<>();
        ReservationWorkflowFacade facade = facade(repository, request -> {
            capturedPayment.set(request);
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });

        ReservationWorkflowResult result = facade.createReservation(rentalRequest(), "token-de-prueba");

        assertEquals("CONFIRMED", result.reservation().getStatus());
        assertEquals(new BigDecimal("300025.00"), result.total());
        assertEquals(1, repository.findAll().size());
        assertEquals("RES-1001", capturedPayment.get().reservationId());
        assertEquals("CUS-7", capturedPayment.get().customerId());
        assertEquals(new BigDecimal("300025.00"), capturedPayment.get().amount());
        assertEquals("token-de-prueba", capturedPayment.get().paymentToken());
    }

    @Test
    void declinedPaymentIsRecordedAsFailedReservation() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = facade(repository,
                request -> new PaymentResult(PaymentResult.Status.DECLINED, "El pago fue rechazado."));

        ReservationWorkflowResult result = facade.createReservation(rentalRequest(), "token-de-prueba");

        assertEquals("FAILED", result.reservation().getStatus());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void unavailableVehiclePreventsPaymentAndReservationCreation() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicReference<PaymentRequest> capturedPayment = new AtomicReference<>();
        ReservationWorkflowFacade facade = facade(repository, request -> {
            capturedPayment.set(request);
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });
        RentalRequest request = rentalRequest(new Vehicle(
                "CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Rented"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> facade.createReservation(request, "token-de-prueba"));

        assertTrue(error.getMessage().contains("no está disponible"));
        assertNull(capturedPayment.get());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void overlappingConfirmedReservationPreventsPayment() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        repository.save(new Reservation("RES-OLD", "CUS-OLD", "CAR-9",
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), "CONFIRMED"));
        AtomicReference<PaymentRequest> capturedPayment = new AtomicReference<>();
        ReservationWorkflowFacade facade = facade(repository, request -> {
            capturedPayment.set(request);
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });

        assertThrows(IllegalStateException.class,
                () -> facade.createReservation(rentalRequest(), "token-de-prueba"));

        assertNull(capturedPayment.get());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rejectsBlankPaymentTokenBeforeCallingGateway() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicReference<PaymentRequest> capturedPayment = new AtomicReference<>();
        ReservationWorkflowFacade facade = facade(repository, request -> {
            capturedPayment.set(request);
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });

        assertThrows(IllegalArgumentException.class,
                () -> facade.createReservation(rentalRequest(), " "));

        assertNull(capturedPayment.get());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void selectedDecoratedServicesAreIncludedInReservationCost() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = facade(repository,
                request -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        ReservationWorkflowResult result = facade.createReservation(
                new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true),
                new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"),
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22),
                "Basic",
                List.of("GPS", "BABY_SEAT"),
                "token-de-prueba"
        );

        assertEquals(new BigDecimal("300050.00"), result.total());
    }

    @Test
    void premiumModeSelectedInInterfaceIncludesPremiumCoverageCost() {
        ReservationWorkflowFacade facade = facade(new InMemoryReservationRepository(),
                request -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        ReservationWorkflowResult result = facade.createReservation(
                new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true),
                new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22),
                "PREMIUM", List.of(), "token-de-prueba");

        assertEquals(new BigDecimal("300056.00"), result.total());
    }

    @Test
    void allRiskModeSelectedInInterfaceIncludesAllRiskCoverageCost() {
        ReservationWorkflowFacade facade = facade(new InMemoryReservationRepository(),
                request -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        ReservationWorkflowResult result = facade.createReservation(
                new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true),
                new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22),
                "ALL_RISK", List.of(), "token-de-prueba");

        assertEquals(new BigDecimal("300100.00"), result.total());
    }

    @Test
    void rejectsUnknownRentalModeInsteadOfSilentlyUsingBasic() {
        ReservationWorkflowFacade facade = facade(new InMemoryReservationRepository(),
                request -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                facade.createReservation(
                        new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true),
                        new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"),
                        LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22),
                        "DELUXE", List.of(), "token-de-prueba"));

        assertTrue(error.getMessage().contains("no está reconocida"));
    }

    @Test
    void rejectsUnknownSelectedServiceBeforeCallingGateway() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicReference<PaymentRequest> capturedPayment = new AtomicReference<>();
        ReservationWorkflowFacade facade = facade(repository, request -> {
            capturedPayment.set(request);
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });

        assertThrows(IllegalArgumentException.class, () -> facade.createReservation(
                new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true),
                new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"),
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 22),
                "Basic",
                List.of("INVALID_SERVICE"),
                "token-de-prueba"
        ));

        assertNull(capturedPayment.get());
        assertTrue(repository.findAll().isEmpty());
    }

    private ReservationWorkflowFacade facade(InMemoryReservationRepository repository, PaymentGateway gateway) {
        return new ReservationWorkflowFacade(
                repository,
                new VehicleAvailabilityService(repository),
                new RentalCostCalculator(),
                gateway
        );
    }

    private RentalRequest rentalRequest() {
        return rentalRequest(new Vehicle("CAR-9", "Toyota", "Corolla", "Sedan", 2024, 150000, "Available"));
    }

    private RentalRequest rentalRequest(Vehicle vehicle) {
        return new RentalRequest.Builder()
                .customer(new Customer("CUS-7", "Cliente de prueba", "cliente@example.com", true))
                .vehicle(vehicle)
                .dates(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22))
                .rentalModeFactory(new BasicModeFactory())
                .build();
    }
}
