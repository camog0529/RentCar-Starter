package edu.rentcar.model.composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServicePackage implements RentalServiceComponent {
    private final String name;
    private final List<RentalServiceComponent> components = new ArrayList<>();

    public ServicePackage(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del paquete es obligatorio.");
        }
        this.name = name;
    }

    public void add(RentalServiceComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("El componente no puede ser nulo.");
        }
        components.add(component);
    }
    public List<RentalServiceComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder(name).append(": ");

        for (int i = 0; i< components.size(); i++) {
            if (i>0) {
                description.append(", ");
            }
            description.append(components.get(i).getDescription());
        }

        return description.toString();
    }

    @Override
    public BigDecimal getCost() {
        BigDecimal total = BigDecimal.ZERO;

        for (RentalServiceComponent component : components) {
            total = total.add(component.getCost());
        }

        return total;
    }
}
