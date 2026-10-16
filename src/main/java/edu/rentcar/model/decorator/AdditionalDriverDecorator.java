package edu.rentcar.model.decorator;

/**
 * Decorador concreto que añade el permiso para un Conductor Adicional.
 */
public class AdditionalDriverDecorator extends RentalDecorator {

    public AdditionalDriverDecorator(Rentable rentable) {
        super(rentable);
    }

    @Override
    public String getDescription() {
        return rentable.getDescription() + " + Additional Authorized Driver Allowance";
    }

    @Override
    public double getDailyRate() {
        return rentable.getDailyRate() + 12.00; // Añade $12.00 diarios al costo
    }
}