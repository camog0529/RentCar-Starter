package edu.rentcar.payment;

/** Resultado interno de un intento de pago, independiente del proveedor. */
public record PaymentResult(Status status, String message) {
    public enum Status {
        APPROVED,
        DECLINED,
        FAILED
    }

    public PaymentResult {
        if (status == null) {
            throw new IllegalArgumentException("Se requiere el estado del pago.");
        }
        message = message == null ? "" : message;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }
}
