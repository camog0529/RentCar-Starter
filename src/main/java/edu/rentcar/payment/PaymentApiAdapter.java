package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;

/** Adapta las solicitudes de pago de RentCar al cliente de la API suministrado. */
public final class PaymentApiAdapter implements PaymentGateway {
    private final PaymentApiClient paymentApiClient;

    public PaymentApiAdapter(String apiBaseUrl) {
        this(new PaymentApiClient(apiBaseUrl));
    }

    public PaymentApiAdapter(PaymentApiClient paymentApiClient) {
        if (paymentApiClient == null) {
            throw new IllegalArgumentException("Se requiere el cliente de la API de pagos.");
        }
        this.paymentApiClient = paymentApiClient;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Se requiere la solicitud de pago.");
        }

        PaymentApiRequest apiRequest = new PaymentApiRequest(
                request.reservationId(),
                request.customerId(),
                request.amount().doubleValue(),
                request.paymentToken()
        );

        try {
            PaymentApiResponse apiResponse = paymentApiClient.pay(apiRequest);
            if (apiResponse.isApproved()) {
                return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
            }
            if (apiResponse.isDeclined()) {
                return new PaymentResult(PaymentResult.Status.DECLINED, "El pago fue rechazado.");
            }
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "El servicio de pagos no pudo completar la operación.");
        } catch (IOException exception) {
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "No fue posible comunicarse con el servicio de pagos.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "La solicitud de pago fue interrumpida.");
        }
    }
}
