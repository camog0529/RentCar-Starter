package edu.rentcar.model.decorator;

/**
 * Decorador concreto que añade el servicio de Navegador GPS al vehículo.
 */
public class GpsDecorator extends RentalDecorator {

    public GpsDecorator(Rentable rentable) {
        super(rentable);
    }

    @Override
    public String getDescription() {
        return rentable.getDescription() + " + GPS Navigation System";
    }

    @Override
    public double getDailyRate() {
        return rentable.getDailyRate() + 5.00; // Añade $5.00 diarios al costo
    }
}