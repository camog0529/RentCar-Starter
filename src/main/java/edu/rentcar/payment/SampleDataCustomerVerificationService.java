package edu.rentcar.payment;

import edu.rentcar.model.Customer;

import java.util.List;
import java.util.function.Supplier;

public final class SampleDataCustomerVerificationService
        implements CustomerVerificationService {

    private final Supplier<List<Customer>> customersProvider;

    public SampleDataCustomerVerificationService(List<Customer> customers) {
        if (customers == null) {
            throw new IllegalArgumentException(
                    "La lista de clientes para verificar no puede ser nula.");
        }
        List<Customer> verifiedCustomers = List.copyOf(customers);
        this.customersProvider = () -> verifiedCustomers;
    }

    /** Consulta la lista vigente para reconocer clientes registrados durante la ejecución. */
    public SampleDataCustomerVerificationService(Supplier<List<Customer>> customersProvider) {
        if (customersProvider == null) {
            throw new IllegalArgumentException(
                    "El proveedor de clientes para verificar no puede ser nulo.");
        }
        this.customersProvider = customersProvider;
    }

    @Override
    public boolean isAuthorized(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return false;
        }

        List<Customer> customers = customersProvider.get();
        if (customers == null) {
            return false;
        }

        return customers.stream()
                .filter(customer -> customer.getId().equals(customerId))
                .findFirst()
                .map(Customer::isAuthorized)
                .orElse(false);
    }
}
