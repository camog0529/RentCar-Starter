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
            throw new IllegalArgumentException("No se puede crear el adaptador: el cliente de la API de pagos es nulo.");
        }
        this.paymentApiClient = paymentApiClient;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No se puede procesar el pago: la solicitud recibida es nula.");
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
                    "El servicio de pagos respondió con HTTP " + apiResponse.httpStatus()
                            + " y no confirmó la aprobación del pago.");
        } catch (IOException exception) {
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "No se pudo establecer comunicación con la API de pagos; verifique que el servicio esté disponible.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "El envío del pago fue interrumpido antes de recibir una respuesta de la API.");
        }
    }
}
