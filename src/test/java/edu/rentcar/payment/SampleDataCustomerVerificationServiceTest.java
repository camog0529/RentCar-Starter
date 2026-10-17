package edu.rentcar.payment;

import edu.rentcar.data.SampleData;
import edu.rentcar.model.Customer;
import edu.rentcar.repository.InMemoryCustomerRepository;
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

    @Test
    void recognizesNewlyRegisteredAuthorizedCustomer() throws Exception {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        SampleDataCustomerVerificationService repositoryService =
                new SampleDataCustomerVerificationService(repository::findAll);
        repository.save(new Customer(
                "CUS-NEW-AUTHORIZED", "Cliente Nuevo", "nuevo@example.com", true));

        assertTrue(repositoryService.isAuthorized("CUS-NEW-AUTHORIZED"));
    }

    @Test
    void blocksNewlyRegisteredUnauthorizedCustomer() throws Exception {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        SampleDataCustomerVerificationService repositoryService =
                new SampleDataCustomerVerificationService(repository::findAll);
        repository.save(new Customer(
                "CUS-NEW-UNAUTHORIZED", "Cliente No Autorizado", "no@example.com", false));

        assertFalse(repositoryService.isAuthorized("CUS-NEW-UNAUTHORIZED"));
    }
}
