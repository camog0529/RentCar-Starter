package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Asistencia en Carretera Premium.
 */
public class PremiumAssistance implements RoadsideAssistance {

    @Override
    public String getAssistanceLevel() {
        return "Premium Assistance: 24/7 technical support, flat tire change service, and emergency fuel delivery.";
    }

    @Override
    public boolean IsFullService() {
        return false; // Aunque es avanzado, no cubre la totalidad de imprevistos extremos
    }
}