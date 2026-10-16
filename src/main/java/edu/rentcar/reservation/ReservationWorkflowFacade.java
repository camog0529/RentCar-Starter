package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;
import edu.rentcar.payment.PaymentGateway;
import edu.rentcar.payment.PaymentRequest;
import edu.rentcar.payment.PaymentResult;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/** Coordinates validation, availability, pricing, payment, and reservation recording. */
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
            throw new IllegalArgumentException("Reservation workflow dependencies are required.");
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
            throw new IllegalStateException("The selected vehicle is not available for these dates.");
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
            throw new IllegalArgumentException("Rental request is required.");
        }
        if (request.getCustomer() == null || request.getVehicle() == null
                || request.getStartDate() == null || request.getEndDate() == null
                || request.getRentalModeFactory() == null) {
            throw new IllegalArgumentException("Rental request is incomplete.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment token is required.");
        }
    }
}
