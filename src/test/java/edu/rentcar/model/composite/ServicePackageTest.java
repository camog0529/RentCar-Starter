package edu.rentcar.model.composite;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicePackageTest {

    @Test
    void simplePackageReturnsAllDescriptionsAndCombinedCost() {
        ServicePackage simplePackage = new ServicePackage("Paquete simple");
        simplePackage.add(new IndividualRentalService("GPS", new BigDecimal("5.00")));
        simplePackage.add(new IndividualRentalService("Silla para bebé", new BigDecimal("7.50")));

        assertEquals("Paquete simple: GPS, Silla para bebé", simplePackage.getDescription());
        assertEquals(new BigDecimal("12.50"), simplePackage.getCost());
    }

    @Test
    void nestedPackageIncludesNestedDescriptionsAndAllCosts() {
        ServicePackage navigationPackage = new ServicePackage("Navegación");
        navigationPackage.add(new IndividualRentalService("GPS", new BigDecimal("5.00")));
        navigationPackage.add(new IndividualRentalService("Mapas sin conexión", new BigDecimal("2.50")));

        ServicePackage familyPackage = new ServicePackage("Familiar");
        familyPackage.add(navigationPackage);
        familyPackage.add(new IndividualRentalService("Silla para bebé", new BigDecimal("7.50")));

        assertEquals("Familiar: Navegación: GPS, Mapas sin conexión, Silla para bebé",
                familyPackage.getDescription());
        assertEquals(new BigDecimal("15.00"), familyPackage.getCost());
    }

    @Test
    void rejectsInvalidLeavesAndNullComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> new IndividualRentalService(" ", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new IndividualRentalService("Servicio", new BigDecimal("-1")));

        ServicePackage servicePackage = new ServicePackage("Paquete");
        assertThrows(IllegalArgumentException.class, () -> servicePackage.add(null));
        assertThrows(UnsupportedOperationException.class, () -> servicePackage.getComponents().clear());
    }
}
