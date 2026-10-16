package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Asistencia en Carretera Completa Todo Riesgo.
 */
public class AllRiskAssistance implements RoadsideAssistance {

    @Override
    public String getAssistanceLevel() {
        return "All Risk Assistance: VIP 24/7 Full Support. Includes immediate replacement vehicle on-site and hotel coverage.";
    }

    @Override
    public boolean IsFullService() {
        return true; // Brinda el servicio completo y absoluto sin restricciones de contrato
    }
}