package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;
import edu.rentcar.payment.PaymentResult;

import java.math.BigDecimal;

/** Outcome returned to the controller after the reservation workflow. */
public record ReservationWorkflowResult(
        Reservation reservation,
        BigDecimal total,
        PaymentResult paymentResult
) {
}
