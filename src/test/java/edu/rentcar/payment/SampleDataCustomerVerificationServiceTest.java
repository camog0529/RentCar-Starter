package edu.rentcar.payment;

import edu.rentcar.data.SampleData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SampleDataCustomerVerificationServiceTest {
    private final SampleDataCustomerVerificationService service =
            new SampleDataCustomerVerificationService(SampleData.customers());

    @Test
    void authorizesKnownCustomerWithAuthorization() throws Exception {
        assertTrue(service.isAuthorized("CUS-1001"));
    }

    @Test
    void deniesKnownCustomerWithoutAuthorization() throws Exception {
        assertFalse(service.isAuthorized("CUS-1003"));
    }

    @Test
    void deniesUnknownOrBlankCustomerId() throws Exception {
        assertFalse(service.isAuthorized("CUS-UNKNOWN"));
        assertFalse(service.isAuthorized(" "));
        assertFalse(service.isAuthorized(null));
    }
}
