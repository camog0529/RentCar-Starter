package edu.rentcar.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PaymentGatewayProxyTest {
    private final PaymentRequest request = new PaymentRequest(
            "RES-1001", "CUS-7", new BigDecimal("125.00"), "token-de-prueba");

    @Test
    void authorizedCustomerIsVerifiedAndPaymentIsDelegated() {
        AtomicReference<String> verifiedCustomerId = new AtomicReference<>();
        AtomicReference<PaymentRequest> delegatedRequest = new AtomicReference<>();
        PaymentResult approved = new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            delegatedRequest.set(payment);
            return approved;
        }, customerId -> {
            verifiedCustomerId.set(customerId);
            return true;
        });

        PaymentResult result = proxy.process(request);

        assertSame(approved, result);
        assertEquals("CUS-7", verifiedCustomerId.get());
        assertSame(request, delegatedRequest.get());
    }

    @Test
    void unauthorizedCustomerIsDeclinedWithoutCallingGateway() {
        AtomicInteger gatewayCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            gatewayCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> false);

        PaymentResult result = proxy.process(request);

        assertEquals(PaymentResult.Status.DECLINED, result.status());
        assertTrue(result.message().contains("Cliente no autorizado"));
        assertEquals(0, gatewayCalls.get());
    }

    @Test
    void verificationErrorReturnsFailureWithoutCallingGateway() {
        AtomicInteger gatewayCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            gatewayCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> {
            throw new IllegalStateException("Error de verificación de prueba");
        });

        PaymentResult result = proxy.process(request);

        assertEquals(PaymentResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("No fue posible verificar"));
        assertEquals(0, gatewayCalls.get());
    }

    @Test
    void interruptedVerificationPreservesInterruptAndDoesNotCallGateway() {
        AtomicInteger gatewayCalls = new AtomicInteger();
        PaymentGatewayProxy proxy = new PaymentGatewayProxy(payment -> {
            gatewayCalls.incrementAndGet();
            return new PaymentResult(PaymentResult.Status.APPROVED, "Pago aprobado.");
        }, customerId -> {
            throw new InterruptedException("interrupción de prueba");
        });

        try {
            PaymentResult result = proxy.process(request);
            assertEquals(PaymentResult.Status.FAILED, result.status());
            assertTrue(Thread.currentThread().isInterrupted());
            assertEquals(0, gatewayCalls.get());
        } finally {
            Thread.interrupted();
        }
    }
}
