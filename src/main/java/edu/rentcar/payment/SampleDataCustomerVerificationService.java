package edu.rentcar.payment;

import edu.rentcar.model.Customer;

import java.util.List;

public final class SampleDataCustomerVerificationService
        implements CustomerVerificationService {

    private final List<Customer> customers;

    public SampleDataCustomerVerificationService(List<Customer> customers) {
        if (customers == null) {
            throw new IllegalArgumentException(
                    "La lista de clientes para verificar no puede ser nula.");
        }
        this.customers = List.copyOf(customers);
    }

    @Override
    public boolean isAuthorized(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return false;
        }

        return customers.stream()
                .filter(customer -> customer.getId().equals(customerId))
                .findFirst()
                .map(Customer::isAuthorized)
                .orElse(false);
    }
}