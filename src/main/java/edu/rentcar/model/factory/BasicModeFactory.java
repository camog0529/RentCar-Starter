package edu.rentcar.model.factory;

/**
 * Fábrica Concreta para la Modalidad Básica.
 * Asegura la creación cohesiva y emparejada de los servicios en su nivel básico.
 */
public class BasicModeFactory implements RentalModeFactory {

    /**
     * @return Una nueva instancia de seguro básico.
     */
    @Override
    public InsuranceCoverage createInsuranceCoverage() {
        return new BasicInsurance();
    }

    /**
     * @return Una nueva instancia de asistencia básica.
     */
    @Override
    public RoadsideAssistance createRoadsideAssistance() {
        return new BasicAssistance();
    }
}