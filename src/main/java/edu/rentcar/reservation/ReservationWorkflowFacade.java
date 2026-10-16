package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/** Coordina la validación, disponibilidad, tarifa, pago y registro de la reservación. */
public final class ReservationWorkflowFacade {
    private final ReservationRepository reservationRepository;
    private final VehicleAvailabilityService availabilityService;
    private final RentalCostCalculator costCalculator;
    private final PaymentGateway paymentGateway;
    private final AtomicLong nextReservationNumber;

    public ReservationWorkflowFacade(
            ReservationRepository reservationRepository,
            VehicleAvailabilityService availabilityService,
            RentalCostCalculator costCalculator,
            PaymentGateway paymentGateway
    ) {
        if (reservationRepository == null || availabilityService == null
                || costCalculator == null || paymentGateway == null) {
            throw new IllegalArgumentException("Los componentes del flujo de reservación son obligatorios.");
        }
        this.reservationRepository = reservationRepository;
        this.availabilityService = availabilityService;
        this.costCalculator = costCalculator;
        this.paymentGateway = paymentGateway;
        this.nextReservationNumber = new AtomicLong(1001);
    }

    public ReservationWorkflowResult createReservation(RentalRequest request, String paymentToken) {
        validate(request, paymentToken);
        if (!availabilityService.isAvailable(request)) {
            throw new IllegalStateException("El vehículo seleccionado no está disponible para las fechas solicitadas.");
        }

        String reservationId = "RES-" + nextReservationNumber.getAndIncrement();
        BigDecimal total = costCalculator.calculate(request);
        PaymentResult paymentResult = paymentGateway.process(new PaymentRequest(
                reservationId,
                request.getCustomer().getId(),
                total,
                paymentToken
        ));

        String status = paymentResult.isApproved() ? "CONFIRMED" : "FAILED";
        Reservation reservation = new Reservation(
                reservationId,
                request.getCustomer().getId(),
                request.getVehicle().getId(),
                request.getStartDate(),
                request.getEndDate(),
                status
        );
        reservationRepository.save(reservation);
        return new ReservationWorkflowResult(reservation, total, paymentResult);
    }

    private void validate(RentalRequest request, String paymentToken) {
        if (request == null) {
            throw new IllegalArgumentException("Se requiere la solicitud de alquiler.");
        }
        if (request.getCustomer() == null || request.getVehicle() == null
                || request.getStartDate() == null || request.getEndDate() == null
                || request.getRentalModeFactory() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler está incompleta.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("La fecha inicial debe ser anterior a la fecha final.");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Se requiere el token de pago.");
        }
    }
}
