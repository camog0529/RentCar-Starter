package edu.rentcar.payment;

/** Contrato interno de la aplicación para procesar el pago de un alquiler. */
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
}
