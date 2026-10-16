package edu.rentcar.model.decorator;

import edu.rentcar.model.Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalDecoratorTest {

    @Test
    void decoratorsAccumulateDescriptionsAndDailyRates() {
        Rentable vehicle = new Vehicle("CAR-1", "Toyota", "Corolla", "Economy", 2024, 100.00, "Available");
        Rentable withGps = new GpsDecorator(vehicle);
        Rentable withGpsAndSeat = new BabySeatDecorator(withGps);
        Rentable withAllExtras = new AdditionalDriverDecorator(withGpsAndSeat);

        assertEquals(124.50, withAllExtras.getDailyRate());
        assertEquals("Toyota Corolla(Economy) + GPS Navigation System + Child Safety Baby Seat"
                + " + Additional Authorized Driver Allowance", withAllExtras.getDescription());
    }

    @Test
    void decoratorRejectsNullComponent() {
        assertThrows(IllegalArgumentException.class, () -> new GpsDecorator(null));
        assertThrows(IllegalArgumentException.class, () -> new BabySeatDecorator(null));
        assertThrows(IllegalArgumentException.class, () -> new AdditionalDriverDecorator(null));
    }
}
