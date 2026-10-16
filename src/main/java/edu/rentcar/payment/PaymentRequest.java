package edu.rentcar.payment;

import java.math.BigDecimal;

/** Datos de pago expresados en el modelo interno de RentCar. */
public record PaymentRequest(
        String reservationId,
        String customerId,
        BigDecimal amount,
        String paymentToken
) {
    public PaymentRequest {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("Se requiere el identificador de la reservación.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Se requiere el identificador del cliente.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El valor del pago debe ser mayor que cero.");
        }
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Se requiere el token de pago.");
        }
    }

    @Override
    public String toString() {
        return "SolicitudPago[idReservacion=" + reservationId
                + ", idCliente=" + customerId
                + ", valor=" + amount
                + ", tokenPago=[OCULTO]]";
    }
}
