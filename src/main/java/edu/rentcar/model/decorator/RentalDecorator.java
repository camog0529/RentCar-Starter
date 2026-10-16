package edu.rentcar.model.decorator;

/**
 * Clase abstracta base para todos los decoradores de servicios adicionales.
 * Mantiene una referencia al componente 'Rentable' que está envolviendo.
 */
public abstract class RentalDecorator implements Rentable {
    protected final Rentable rentable; // El objeto envuelto (puede ser un vehículo u otro decorador)

    public RentalDecorator(Rentable rentable) {
        if (rentable == null) {
            throw new IllegalArgumentException("The rentable object to decorate cannot be null.");
        }
        this.rentable = rentable;
    }

    @Override
    public String getDescription() {
        return rentable.getDescription(); // Delega el comportamiento por defecto
    }

    @Override
    public double getDailyRate() {
        return rentable.getDailyRate(); // Delega el comportamiento por defecto
    }
}