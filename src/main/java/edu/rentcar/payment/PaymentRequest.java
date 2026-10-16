package edu.rentcar.payment;

import java.math.BigDecimal;

/** Payment data expressed in RentCar's application model. */
public record PaymentRequest(
        String reservationId,
        String customerId,
        BigDecimal amount,
        String paymentToken
) {
    public PaymentRequest {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment token is required.");
        }
    }

    @Override
    public String toString() {
        return "PaymentRequest[reservationId=" + reservationId
                + ", customerId=" + customerId
                + ", amount=" + amount
                + ", paymentToken=[REDACTED]]";
    }
}
