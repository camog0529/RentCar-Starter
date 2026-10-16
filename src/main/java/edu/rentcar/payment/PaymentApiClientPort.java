package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;

/** Contrato mínimo para comunicarse con la biblioteca externa de pagos. */
@FunctionalInterface
public interface PaymentApiClientPort {
    PaymentApiResponse pay(PaymentApiRequest request) throws IOException, InterruptedException;
}
