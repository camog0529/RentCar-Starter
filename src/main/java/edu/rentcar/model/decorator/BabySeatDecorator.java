package edu.rentcar.model.decorator;

/**
 * Decorador concreto que añade una Silla de Bebé de seguridad al vehículo.
 */
public class BabySeatDecorator extends RentalDecorator {

    public BabySeatDecorator(Rentable rentable) {
        super(rentable);
    }

    @Override
    public String getDescription() {
        return rentable.getDescription() + " + Child Safety Baby Seat";
    }

    @Override
    public double getDailyRate() {
        return rentable.getDailyRate() + 7.50; // Añade $7.50 diarios al costo
    }
}