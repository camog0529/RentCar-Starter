package edu.rentcar.payment;

/** Provider-independent result of a payment attempt. */
public record PaymentResult(Status status, String message) {
    public enum Status {
        APPROVED,
        DECLINED,
        FAILED
    }

    public PaymentResult {
        if (status == null) {
            throw new IllegalArgumentException("Payment status is required.");
        }
        message = message == null ? "" : message;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }
}
