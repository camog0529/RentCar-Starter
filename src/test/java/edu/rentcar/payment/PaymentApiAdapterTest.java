package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PaymentApiAdapterTest {

    @Test
    void mapsInternalPaymentToApiRequestAndReturnsApproval() {
        AtomicReference<PaymentApiRequest> capturedRequest = new AtomicReference<>();
        PaymentApiAdapter adapter = new PaymentApiAdapter(request -> {
            capturedRequest.set(request);
            return new PaymentApiResponse(200, "APPROVED");
        });

        PaymentResult result = adapter.process(paymentRequest());

        assertEquals(PaymentResult.Status.APPROVED, result.status());
        assertEquals("Pago aprobado.", result.message());
        assertEquals("RES-1001", capturedRequest.get().reservationId());
        assertEquals("CUS-7", capturedRequest.get().customerId());
        assertEquals(125.75, capturedRequest.get().amount());
        assertEquals("token-de-prueba", capturedRequest.get().paymentToken());
    }

    @Test
    void returnsDeclinedWhenProviderRejectsPayment() {
        PaymentApiAdapter adapter = new PaymentApiAdapter(
                request -> new PaymentApiResponse(200, "NOT_APPROVED"));

        PaymentResult result = adapter.process(paymentRequest());

        assertEquals(PaymentResult.Status.DECLINED, result.status());
        assertEquals("El pago fue rechazado.", result.message());
    }

    @Test
    void returnsFailureWhenProviderDoesNotConfirmPayment() {
        PaymentApiAdapter adapter = new PaymentApiAdapter(
                request -> new PaymentApiResponse(503, "Service unavailable"));

        PaymentResult result = adapter.process(paymentRequest());

        assertEquals(PaymentResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("HTTP 503"));
    }

    @Test
    void returnsFailureWhenProviderCommunicationThrowsIOException() {
        PaymentApiAdapter adapter = new PaymentApiAdapter(request -> {
            throw new IOException("detalle interno");
        });

        PaymentResult result = adapter.process(paymentRequest());

        assertEquals(PaymentResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("No se pudo establecer comunicación"));
        assertFalse(result.message().contains("detalle interno"));
    }

    @Test
    void preservesThreadInterruptionWhenRequestIsInterrupted() {
        PaymentApiAdapter adapter = new PaymentApiAdapter(request -> {
            throw new InterruptedException("interrupción de prueba");
        });

        try {
            PaymentResult result = adapter.process(paymentRequest());
            assertEquals(PaymentResult.Status.FAILED, result.status());
            assertTrue(result.message().contains("fue interrumpido"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void hidesPaymentTokenWhenRequestIsConvertedToText() {
        String text = paymentRequest().toString();

        assertTrue(text.contains("tokenPago=[OCULTO]"));
        assertFalse(text.contains("token-de-prueba"));
    }

    private PaymentRequest paymentRequest() {
        return new PaymentRequest("RES-1001", "CUS-7", new BigDecimal("125.75"), "token-de-prueba");
    }
}
