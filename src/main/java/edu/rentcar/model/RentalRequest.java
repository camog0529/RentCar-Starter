package edu.rentcar.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import edu.rentcar.model.factory.RentalModeFactory; // Importación de la interfaz del patrón Abstract Factory

/**
 * Clase que representa una Solicitud de Alquiler de Vehículo (RentalRequest).
 * <p>
 * DESIGN PATTERNS APLICADOS:
 * 1. Patrón Creacional BUILDER: Permite construir esta solicitud paso a paso de forma fluida.
 * 2. Patrón Creacional ABSTRACT FACTORY: Integra la fábrica abstracta {@link RentalModeFactory}
 * para delegar la creación de familias de objetos de seguros y asistencias según la modalidad elegida (RF-003).
 * <p>
 * PRINCIPIOS DE INGENIERÍA DE SOFTWARE:
 * - Inmutabilidad: Todos los campos son de tipo 'final'. Una vez construida la solicitud mediante
 * el Builder, sus datos no pueden ser alterados externamente, garantizando la consistencia del negocio.
 */
public class RentalRequest {

    // --- ATRIBUTOS PRIVADOS E INMUTABLES ---
    private final Customer customer;                   // El cliente que realiza la solicitud
    private final Vehicle vehicle;                     // El vehículo que se desea rentar
    private final LocalDate startDate;                 // Fecha en la que inicia el alquiler
    private final LocalDate endDate;                   // Fecha en la que finaliza el alquiler
    private final RentalModeFactory rentalModeFactory; // Fábrica abstracta inyectada que define la modalidad (RF-003)
    private final List<Object> services;               // Colección de servicios adicionales solicitados

    /**
     * Constructor Privado de la clase.
     * Está restringido para que NADIE fuera de esta clase pueda instanciar una solicitud directamente usando 'new'.
     * La única manera de nacer que tiene un objeto RentalRequest es a través del método '.build()' de su Builder interno.
     *
     * @param builder Instancia del constructor que contiene todos los datos validados previamente.
     */
    private RentalRequest(Builder builder) {
        this.customer = builder.customer;
        this.vehicle = builder.vehicle;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.rentalModeFactory = builder.rentalModeFactory;
        this.services = builder.services;
    }

    // --- MÉTODOS ACCESORES (GETTERS) EN INGLÉS ---

    /**
     * @return El cliente asociado a esta solicitud de renta.
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @return El vehículo seleccionado para el alquiler.
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * @return La fecha de inicio del periodo de alquiler.
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return La fecha de finalización del periodo de alquiler.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return La fábrica abstracta que contiene las configuraciones de la modalidad contratada (SOAT, coberturas, etc).
     */
    public RentalModeFactory getRentalModeFactory() {
        return rentalModeFactory;
    }

    /**
     * @return La lista de objetos que representan los servicios complementarios agregados a la solicitud.
     */
    public List<Object> getServices() {
        return services;
    }


    // =========================================================================
    // --- CLASE BUILDER INTERNA Y ESTÁTICA ---
    // =========================================================================

    /**
     * Clase encargada de recolectar las configuraciones, validar las reglas de negocio
     * y ensamblar progresivamente el objeto complejo RentalRequest.
     */
    public static class Builder {
        // Atributos temporales del Builder para almacenar la configuración antes de la validación final
        private Customer customer;
        private Vehicle vehicle;
        private LocalDate startDate;
        private LocalDate endDate;
        private RentalModeFactory rentalModeFactory;
        private List<Object> services = new ArrayList<>();

        /**
         * Asigna el cliente a la solicitud en construcción.
         *
         * @param customer Instancia del cliente.
         * @return La misma instancia del Builder (this) para permitir encadenamiento de métodos.
         */
        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        /**
         * Asigna el vehículo a la solicitud en construcción.
         *
         * @param vehicle Instancia del vehículo de la flota.
         * @return La misma instancia del Builder (this) para permitir encadenamiento de métodos.
         */
        public Builder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        /**
         * Asigna el rango de fechas para el alquiler y ejecuta una validación de negocio fundamental.
         *
         * @param startDate Fecha de inicio del alquiler.
         * @param endDate   Fecha de finalización del alquiler.
         * @return La misma instancia del Builder (this) para permitir encadenamiento de métodos.
         * @throws IllegalArgumentException Si la fecha de inicio NO es cronológicamente anterior a la fecha de fin.
         */
        public Builder dates(LocalDate startDate, LocalDate endDate) {
            // Regla de Negocio Crítica: El alquiler no puede finalizar antes de empezar ni durar 0 días.
            if (startDate != null && endDate != null && !startDate.isBefore(endDate)) {
                throw new IllegalArgumentException("Validation Error: Start date must be strictly before end date.");
            }
            this.startDate = startDate;
            this.endDate = endDate;
            return this;
        }

        /**
         * Inyecta la fábrica concreta correspondiente a la modalidad elegida en la interfaz gráfica
         * (BasicModeFactory, PremiumModeFactory o AllRiskModeFactory). Cumple con el RF-003.
         *
         * @param rentalModeFactory Instancia de la fábrica concreta que maneja los seguros y asistencias.
         * @return La misma instancia del Builder (this) para permitir encadenamiento de métodos.
         */
        public Builder rentalModeFactory(RentalModeFactory rentalModeFactory) {
            this.rentalModeFactory = rentalModeFactory;
            return this;
        }

        /**
         * Añade un servicio opcional de manera individual a la lista de requerimientos de la solicitud.
         *
         * @param service El servicio adicional (ej: GPS, Silla de bebé).
         * @return La misma instancia del Builder (this) para permitir encadenamiento de métodos.
         */
        public Builder addService(Object service) {
            this.services.add(service);
            return this;
        }

        /**
         * MÉTODO DE FABRICACIÓN FINAL
         * Analiza que todos los datos obligatorios del sistema estén completos (RF-001).
         * Si todo es correcto, invoca de manera segura al constructor privado de RentalRequest.
         *
         * @return Un objeto RentalRequest inmutable, completo y consistente con el negocio.
         * @throws IllegalStateException Si el desarrollador intentó compilar la solicitud olvidando ingresar algún dato obligatorio.
         */
        public RentalRequest build() {
            // Validación de Integridad Requerida por el enunciado del parcial
            if (customer == null || vehicle == null || startDate == null || endDate == null || rentalModeFactory == null) {
                throw new IllegalStateException("Business Error: Incomplete rental request data. All parameters are mandatory.");
            }
            return new RentalRequest(this);
        }
    }
}