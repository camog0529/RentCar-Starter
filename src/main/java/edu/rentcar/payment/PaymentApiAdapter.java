package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;

/** Adapts RentCar payment requests to the supplied payment API client. */
public final class PaymentApiAdapter implements PaymentGateway {
    private final PaymentApiClient paymentApiClient;

    public PaymentApiAdapter(String apiBaseUrl) {
        this(new PaymentApiClient(apiBaseUrl));
    }

    public PaymentApiAdapter(PaymentApiClient paymentApiClient) {
        if (paymentApiClient == null) {
            throw new IllegalArgumentException("Payment API client is required.");
        }
        this.paymentApiClient = paymentApiClient;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required.");
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
                return new PaymentResult(PaymentResult.Status.APPROVED, apiResponse.body());
            }
            if (apiResponse.isDeclined()) {
                return new PaymentResult(PaymentResult.Status.DECLINED, apiResponse.body());
            }
            return new PaymentResult(PaymentResult.Status.FAILED, apiResponse.body());
        } catch (IOException exception) {
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "Payment service communication failed.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new PaymentResult(PaymentResult.Status.FAILED,
                    "Payment request was interrupted.");
        }
    }
}
