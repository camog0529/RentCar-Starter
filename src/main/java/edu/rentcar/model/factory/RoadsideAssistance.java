package edu.rentcar.model.factory;

/**
 * Interfaz base que define el comportamiento para la Asistencia en Carretera.
 */
public interface RoadsideAssistance {
    String getAssistanceLevel();
    boolean IsFullService(); // Nota: Puedes cambiar la 'I' a minúscula si prefieres la convención exacta 'isFullService'
}