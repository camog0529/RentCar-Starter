package edu.rentcar;

import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Vehicle;
import edu.rentcar.model.composite.IndividualRentalService;
import edu.rentcar.model.composite.ServicePackage;
import edu.rentcar.model.factory.BasicModeFactory;
import edu.rentcar.model.factory.InsuranceCoverage;
import edu.rentcar.model.factory.PremiumModeFactory;
import edu.rentcar.model.factory.RentalModeFactory;
import edu.rentcar.model.factory.RoadsideAssistance;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentGatewayProxy;
import edu.rentcar.payment.PaymentResult;
import edu.rentcar.reservation.InMemoryReservationRepository;
import edu.rentcar.reservation.RentalCostCalculator;
import edu.rentcar.reservation.ReservationWorkflowFacade;
import edu.rentcar.reservation.VehicleAvailabilityService;
import org.junit.jupiter.api.Test;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas mínimas del parcial para los requisitos RF-001 a RF-007. */
class ReservationProcessTest {

    @Test
    void rf001BuildsRequestWithValidInformation() {
        RentalRequest request = validRequest(true);

        assertEquals("CUS-TEST", request.getCustomer().getId());
        assertEquals("CAR-TEST", request.getVehicle().getId());
        assertEquals(LocalDate.of(2026, 10, 20), request.getStartDate());
        assertEquals(LocalDate.of(2026, 10, 22), request.getEndDate());
    }

    @Test
    void rf001RejectsIncompleteRequest() {
        assertThrows(IllegalStateException.class, () -> new RentalRequest.Builder().build());
    }

    @Test
    void rf001ReversedDatesAreRejectedAsAValidationError() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicInteger paymentCalls = new AtomicInteger();
        ReservationWorkflowFacade facade = createFacade(repository, payment -> {
            paymentCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> facade.createReservation(
                        customer(true), vehicle(),
                        LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 20),
                        "Basic", java.util.List.of(), "token-de-prueba"));

        assertNotNull(error.getMessage());
        assertEquals(0, paymentCalls.get());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rf002SuccessfulRentalIsSavedAsConfirmed() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = createFacade(repository,
                payment -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        var result = facade.createReservation(validRequest(true), "token-de-prueba");

        assertEquals("CONFIRMED", result.reservation().getStatus());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rf002UnavailableVehicleDoesNotCallPaymentOrSaveConfirmedRental() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicInteger paymentCalls = new AtomicInteger();
        ReservationWorkflowFacade facade = createFacade(repository, payment -> {
            paymentCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        });
        RentalRequest request = request(customer(true), vehicle("Rented"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), new BasicModeFactory());

        assertThrows(IllegalStateException.class,
                () -> facade.createReservation(request, "token-de-prueba"));

        assertEquals(0, paymentCalls.get());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rf002DeclinedPaymentIsNotMarkedAsSuccessful() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = createFacade(repository,
                payment -> new PaymentResult(PaymentResult.Status.DECLINED, "El pago fue rechazado."));

        var result = facade.createReservation(validRequest(true), "token-de-prueba");

        assertEquals("FAILED", result.reservation().getStatus());
        assertNotEquals("CONFIRMED", result.reservation().getStatus());
    }

    @Test
    void rf003DifferentModesProduceDifferentCoverageCosts() {
        RentalCostCalculator calculator = new RentalCostCalculator();
        LocalDate start = LocalDate.of(2026, 10, 20);
        LocalDate end = LocalDate.of(2026, 10, 22);

        BigDecimal basicTotal = calculator.calculate(request(
                customer(true), vehicle("Available"), start, end, new BasicModeFactory()));
        BigDecimal premiumTotal = calculator.calculate(request(
                customer(true), vehicle("Available"), start, end, new PremiumModeFactory()));

        assertEquals(new BigDecimal("225.00"), basicTotal);
        assertEquals(new BigDecimal("256.00"), premiumTotal);
        assertNotEquals(basicTotal, premiumTotal);
    }

    @Test
    void rf003CustomModeCanBeAddedThroughTheFactoryContract() {
        RentalModeFactory customMode = new RentalModeFactory() {
            @Override
            public InsuranceCoverage createInsuranceCoverage() {
                return new InsuranceCoverage() {
                    @Override
                    public String getCoverageDetails() {
                        return "Cobertura personalizada";
                    }

                    @Override
                    public double getCoverageCost() {
                        return 3.00;
                    }
                };
            }

            @Override
            public RoadsideAssistance createRoadsideAssistance() {
                return new RoadsideAssistance() {
                    @Override
                    public String getAssistanceLevel() {
                        return "Asistencia personalizada";
                    }

                    @Override
                    public boolean IsFullService() {
                        return false;
                    }
                };
            }
        };
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = createFacade(repository,
                payment -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        var result = facade.createReservation(request(customer(true), vehicle("Available"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), customMode), "token-de-prueba");

        assertEquals(new BigDecimal("206.00"), result.total());
        assertEquals("CONFIRMED", result.reservation().getStatus());
    }

    @Test
    void rf004AdapterHandlesApprovedAndDeclinedPaymentResponses() {
        PaymentRequest request = new PaymentRequest(
                "RES-1", "CUS-TEST", new BigDecimal("100.00"), "token-de-prueba");
        PaymentApiAdapter approvedAdapter = new PaymentApiAdapter(
                payment -> new PaymentApiResponse(200, "APPROVED"));
        PaymentApiAdapter declinedAdapter = new PaymentApiAdapter(
                payment -> new PaymentApiResponse(200, "NOT_APPROVED"));

        assertEquals(PaymentResult.Status.APPROVED, approvedAdapter.process(request).status());
        assertEquals(PaymentResult.Status.DECLINED, declinedAdapter.process(request).status());
    }

    @Test
    void rf004AdapterHandlesUnavailableApiAndConnectionFailure() {
        PaymentRequest request = new PaymentRequest(
                "RES-1", "CUS-TEST", new BigDecimal("100.00"), "token-de-prueba");
        PaymentApiAdapter unavailableAdapter = new PaymentApiAdapter(
                payment -> new PaymentApiResponse(503, "Servicio no disponible"));
        PaymentApiAdapter connectionFailureAdapter = new PaymentApiAdapter(payment -> {
            throw new java.io.IOException("Falla de conexión simulada");
        });

        assertEquals(PaymentResult.Status.FAILED, unavailableAdapter.process(request).status());
        assertEquals(PaymentResult.Status.FAILED, connectionFailureAdapter.process(request).status());
    }

    @Test
    void rf006UnauthorizedCustomerDoesNotReachPaymentGateway() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicInteger externalPaymentCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            externalPaymentCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> false);
        ReservationWorkflowFacade facade = createFacade(repository, proxy);

        var result = facade.createReservation(validRequest(false), "token-de-prueba");

        assertEquals(PaymentResult.Status.DECLINED, result.paymentResult().status());
        assertEquals("FAILED", result.reservation().getStatus());
        assertEquals(0, externalPaymentCalls.get());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rf006AuthorizedCustomerCanContinueToPayment() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicInteger externalPaymentCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            externalPaymentCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> true);
        ReservationWorkflowFacade facade = createFacade(repository, proxy);

        var result = facade.createReservation(validRequest(true), "token-de-prueba");

        assertEquals(PaymentResult.Status.APPROVED, result.paymentResult().status());
        assertEquals("CONFIRMED", result.reservation().getStatus());
        assertEquals(1, externalPaymentCalls.get());
    }

    @Test
    void rf006VerificationErrorFailsSafelyWithoutCallingPaymentGateway() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        AtomicInteger externalPaymentCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            externalPaymentCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> {
            throw new IllegalStateException("Falla de verificación de prueba");
        });
        ReservationWorkflowFacade facade = createFacade(repository, proxy);

        var result = facade.createReservation(validRequest(true), "token-de-prueba");

        assertEquals(PaymentResult.Status.FAILED, result.paymentResult().status());
        assertEquals("FAILED", result.reservation().getStatus());
        assertEquals(0, externalPaymentCalls.get());
    }

    @Test
    void rf005CombinedServicesAppearInFinalDescriptionAndCost() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        ReservationWorkflowFacade facade = createFacade(repository,
                payment -> new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado."));

        var result = facade.createReservation(
                customer(true), vehicle("Available"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22),
                "Basic", java.util.List.of("GPS", "BABY_SEAT"), "token-de-prueba");

        assertEquals("GPS Navigation System, Child Safety Baby Seat", result.servicesDescription());
        assertEquals(new BigDecimal("250.00"), result.total());
    }

    @Test
    void rf007SimpleServicePackageSumsDescriptionAndCost() {
        ServicePackage packageOfServices = new ServicePackage("Paquete básico");
        packageOfServices.add(new IndividualRentalService("GPS", new BigDecimal("5.00")));
        packageOfServices.add(new IndividualRentalService("Silla infantil", new BigDecimal("7.50")));

        assertEquals("Paquete básico: GPS, Silla infantil", packageOfServices.getDescription());
        assertEquals(new BigDecimal("12.50"), packageOfServices.getCost());
    }

    @Test
    void rf007NestedServicePackageSumsAllNestedCosts() {
        ServicePackage comfortPackage = new ServicePackage("Confort");
        comfortPackage.add(new IndividualRentalService("GPS", new BigDecimal("5.00")));
        comfortPackage.add(new IndividualRentalService("Silla infantil", new BigDecimal("7.50")));

        ServicePackage completePackage = new ServicePackage("Completo");
        completePackage.add(comfortPackage);
        completePackage.add(new IndividualRentalService("Conductor adicional", new BigDecimal("12.00")));

        assertEquals("Completo: Confort: GPS, Silla infantil, Conductor adicional",
                completePackage.getDescription());
        assertEquals(new BigDecimal("24.50"), completePackage.getCost());
    }

    private ReservationWorkflowFacade createFacade(
            InMemoryReservationRepository repository,
            edu.rentcar.payment.PaymentGateway paymentGateway
    ) {
        return new ReservationWorkflowFacade(
                repository,
                new VehicleAvailabilityService(repository),
                new RentalCostCalculator(),
                paymentGateway
        );
    }

    private RentalRequest validRequest(boolean authorized) {
        return request(customer(authorized), vehicle("Available"),
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), new BasicModeFactory());
    }

    private RentalRequest request(Customer customer, Vehicle vehicle, LocalDate startDate,
                                  LocalDate endDate, RentalModeFactory mode) {
        return new RentalRequest.Builder()
                .customer(customer)
                .vehicle(vehicle)
                .dates(startDate, endDate)
                .rentalModeFactory(mode)
                .build();
    }

    private Customer customer(boolean authorized) {
        return new Customer("CUS-TEST", "Cliente de prueba", "cliente@example.com", authorized);
    }

    private Vehicle vehicle() {
        return vehicle("Available");
    }

    private Vehicle vehicle(String status) {
        return new Vehicle("CAR-TEST", "Toyota", "Corolla", "Economy", 2024, 100, status);
    }
}
