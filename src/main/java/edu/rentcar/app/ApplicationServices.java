package edu.rentcar.app;

import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.service.VehicleService;
import edu.rentcar.reservation.ReservationWorkflowFacade;
import edu.rentcar.reservation.InMemoryReservationRepository;
import edu.rentcar.reservation.VehicleAvailabilityService;
import edu.rentcar.reservation.RentalCostCalculator;
import edu.rentcar.payment.PaymentApiAdapter;

public final class ApplicationServices {

    private static final VehicleService VEHICLE_SERVICE = new VehicleService(new VehicleRepository());

    // Inicialización de la fachada con todas las dependencias requeridas por su constructor
    private static final InMemoryReservationRepository RESERVATION_REPOSITORY = new InMemoryReservationRepository();
    private static final ReservationWorkflowFacade RESERVATION_WORKFLOW_FACADE = new ReservationWorkflowFacade(
            RESERVATION_REPOSITORY,
            new VehicleAvailabilityService(RESERVATION_REPOSITORY),
            new RentalCostCalculator(),
            new PaymentApiAdapter()
    );

    private ApplicationServices() {}

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }

    // Este es el método que necesita el controlador para dejar de estar en rojo
    public static ReservationWorkflowFacade reservationWorkflowFacade() {
        return RESERVATION_WORKFLOW_FACADE;
    }
}