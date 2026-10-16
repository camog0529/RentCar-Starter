package edu.rentcar.model.factory;

/**
 * Implementación concreta de la Asistencia en Carretera Básica.
 */
public class BasicAssistance implements RoadsideAssistance {

    /**
     * @return El alcance del servicio de asistencia básica.
     */
    @Override
    public String getAssistanceLevel() {
        return "Basic Assistance: Standard tow truck service limited up to a 30 km radius. Standard phone support.";
    }

    /**
     * @return false ya que esta modalidad no incluye soporte completo ilimitado.
     */
    @Override
    public boolean IsFullService() {
        return false;
    }
}