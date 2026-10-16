package edu.rentcar.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void rejectsMissingIdentifiersInvalidAmountAndMissingToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRequest(" ", "CUS-1", BigDecimal.ONE, "token"));
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRequest("RES-1", " ", BigDecimal.ONE, "token"));
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRequest("RES-1", "CUS-1", BigDecimal.ZERO, "token"));
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentRequest("RES-1", "CUS-1", BigDecimal.ONE, " "));
    }

    @Test
    void neverIncludesSensitivePaymentTokenInText() {
        PaymentRequest request = new PaymentRequest(
                "RES-1", "CUS-1", BigDecimal.TEN, "secreto-no-mostrar");

        assertTrue(request.toString().contains("tokenPago=[OCULTO]"));
        assertFalse(request.toString().contains("secreto-no-mostrar"));
    }
}
