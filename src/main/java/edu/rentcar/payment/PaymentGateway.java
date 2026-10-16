package edu.rentcar.payment;

/** Application-facing contract for processing a rental payment. */
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
}
