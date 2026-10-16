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
            throw new IllegalArgumentException("No se puede iniciar el flujo: debe proporcionar el repositorio de reservaciones, "
                    + "el verificador de disponibilidad, la calculadora de costos y la pasarela de pago.");
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
            throw new IllegalStateException("No se puede crear la reservación: el vehículo "
                    + request.getVehicle().getId() + " no está disponible del "
                    + request.getStartDate() + " al " + request.getEndDate() + ".");
        }

        String reservationId = "RES-" + nextReservationNumber.getAndIncrement();
        BigDecimal total = costCalculator.calculate(request);
        PaymentResult paymentResult = paymentGateway.process(new PaymentRequest(
                reservationId,
                request.getCustomer().getId(),
                total,
                paymentToken
        ));
        if (paymentResult == null) {
            throw new IllegalStateException("La pasarela de pago no devolvió un resultado para la reservación "
                    + reservationId + ".");
        }

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
            throw new IllegalArgumentException("No se puede iniciar la reservación: la solicitud de alquiler es nula.");
        }
        if (request.getCustomer() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler no tiene un cliente asociado.");
        }
        if (request.getVehicle() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler no tiene un vehículo seleccionado.");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler no tiene una fecha de inicio.");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler no tiene una fecha de finalización.");
        }
        if (request.getRentalModeFactory() == null) {
            throw new IllegalArgumentException("La solicitud de alquiler no tiene una modalidad seleccionada.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio " + request.getStartDate()
                    + " debe ser anterior a la fecha de finalización " + request.getEndDate() + ".");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("No se puede enviar el pago: falta el token de pago de prueba.");
        }
    }
}
