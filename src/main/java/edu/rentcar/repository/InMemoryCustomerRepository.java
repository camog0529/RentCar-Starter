package edu.rentcar.repository;

import edu.rentcar.data.SampleData;
import edu.rentcar.model.Customer;
import java.util.ArrayList;
import java.util.List;

public class InMemoryCustomerRepository {
    // Esta lista alojará de forma definitiva todos los clientes del sistema en ejecución
    private List<Customer> customers = new ArrayList<>();

    public InMemoryCustomerRepository() {
        // Al nacer el repositorio, migramos los datos estáticos de muestra a la memoria dinámica
        customers.addAll(SampleData.customers());
    }

    public List<Customer> findAll() {
        return customers;
    }

    public void save(Customer customer) {
        customers.add(customer);
    }
}