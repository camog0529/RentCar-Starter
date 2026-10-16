package edu.rentcar.model.composite;

import java.math.BigDecimal;

public class IndividualRentalService implements RentalServiceComponent {
    private final String description;
    private final BigDecimal cost;

    public IndividualRentalService(String description, BigDecimal cost) {
        if(description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripcion del servicio es obligatoria.");
        }
        if (cost == null || cost.signum()<0) {
            throw new IllegalArgumentException("El costo debe ser cero o mayor.");
        }
        this.description = description;
        this.cost = cost;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal getCost() {
        return cost;
    }
}
