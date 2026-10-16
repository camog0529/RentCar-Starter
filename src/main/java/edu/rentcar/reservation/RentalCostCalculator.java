package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/** Calcula el costo base del alquiler y la cobertura de la modalidad seleccionada. */
public final class RentalCostCalculator {
    public BigDecimal calculate(RentalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No se puede calcular el costo: la solicitud de alquiler es nula.");
        }
        if (request.getVehicle() == null || request.getStartDate() == null
                || request.getEndDate() == null || request.getRentalModeFactory() == null) {
            throw new IllegalArgumentException("No se puede calcular el costo: faltan el vehículo, las fechas o la modalidad.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("No se puede calcular el costo: la fecha de inicio debe ser anterior a la fecha final.");
        }
        long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal dailyVehicleRate = BigDecimal.valueOf(request.getVehicle().getDailyRate());
        BigDecimal dailyCoverageRate = BigDecimal.valueOf(
                request.getRentalModeFactory().createInsuranceCoverage().getCoverageCost());

        return dailyVehicleRate.add(dailyCoverageRate)
                .multiply(BigDecimal.valueOf(rentalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
