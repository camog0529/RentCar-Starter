package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/** Calculates base rental and selected mode coverage costs. */
public final class RentalCostCalculator {
    public BigDecimal calculate(RentalRequest request) {
        long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal dailyVehicleRate = BigDecimal.valueOf(request.getVehicle().getDailyRate());
        BigDecimal dailyCoverageRate = BigDecimal.valueOf(
                request.getRentalModeFactory().createInsuranceCoverage().getCoverageCost());

        return dailyVehicleRate.add(dailyCoverageRate)
                .multiply(BigDecimal.valueOf(rentalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
