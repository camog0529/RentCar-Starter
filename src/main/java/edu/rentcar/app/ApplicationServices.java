package edu.rentcar.app;

import edu.rentcar.data.SampleData;
import edu.rentcar.payment.PaymentApiAdapter;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentGatewayProxy;
import edu.rentcar.payment.SampleDataCustomerVerificationService;
import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.reservation.InMemoryReservationRepository;
import edu.rentcar.reservation.RentalCostCalculator;
import edu.rentcar.reservation.ReservationRepository;
import edu.rentcar.reservation.ReservationWorkflowFacade;
import edu.rentcar.reservation.VehicleAvailabilityService;
import edu.rentcar.service.VehicleService;

/**
 * ApplicationServices es un Service Locator: los controllers consultan una clase global para obtener los servicios.
 */
public final class ApplicationServices {

    private static final VehicleService VEHICLE_SERVICE =
            new VehicleService(new VehicleRepository());

    private static final ReservationRepository RESERVATION_REPOSITORY =
            new InMemoryReservationRepository();

    private static final PaymentGateway PAYMENT_GATEWAY =
            new PaymentGatewayProxy(
                    new PaymentApiAdapter("http://localhost:8080"),
                    new SampleDataCustomerVerificationService(SampleData.customers())
            );

    private static final ReservationWorkflowFacade RESERVATION_WORKFLOW_FACADE =
            new ReservationWorkflowFacade(
                    RESERVATION_REPOSITORY,
                    new VehicleAvailabilityService(RESERVATION_REPOSITORY),
                    new RentalCostCalculator(),
                    PAYMENT_GATEWAY
            );


    private ApplicationServices() {}

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }


    public static ReservationWorkflowFacade reservationWorkflowFacade() {
        return RESERVATION_WORKFLOW_FACADE;
    }
}