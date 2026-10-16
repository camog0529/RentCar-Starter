package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;

/** Comprueba cruces de fechas con reservaciones confirmadas en esta ejecución. */
public final class VehicleAvailabilityService {
    private final ReservationRepository reservationRepository;

    public VehicleAvailabilityService(ReservationRepository reservationRepository) {
        if (reservationRepository == null) {
            throw new IllegalArgumentException("El repositorio de reservaciones es obligatorio.");
        }
        this.reservationRepository = reservationRepository;
    }

    public boolean isAvailable(RentalRequest request) {
        if (!"Available".equalsIgnoreCase(request.getVehicle().getStatus())) {
            return false;
        }
        return reservationRepository.findAll().stream()
                .filter(reservation -> "CONFIRMED".equals(reservation.getStatus()))
                .filter(reservation -> reservation.getVehicleId().equals(request.getVehicle().getId()))
                .noneMatch(reservation -> overlaps(request, reservation));
    }

    private boolean overlaps(RentalRequest request, Reservation reservation) {
        return request.getStartDate().isBefore(reservation.getEndDate())
                && reservation.getStartDate().isBefore(request.getEndDate());
    }
}
