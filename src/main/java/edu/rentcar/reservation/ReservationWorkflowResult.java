package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;
import edu.rentcar.payment.PaymentResult;

import java.math.BigDecimal;

/** Resultado que se devuelve al controlador al terminar el flujo de reservación. */
public record ReservationWorkflowResult(
        Reservation reservation,
        BigDecimal total,
        PaymentResult paymentResult
) {
}
