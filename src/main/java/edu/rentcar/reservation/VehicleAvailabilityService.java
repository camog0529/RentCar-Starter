package edu.rentcar.reservation;

import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Reservation;

/** Comprueba cruces de fechas con reservaciones confirmadas en esta ejecución. */
public final class VehicleAvailabilityService {
    private final ReservationRepository reservationRepository;

    public VehicleAvailabilityService(ReservationRepository reservationRepository) {
        if (reservationRepository == null) {
            throw new IllegalArgumentException("No se puede verificar disponibilidad sin un repositorio de reservaciones.");
        }
        this.reservationRepository = reservationRepository;
    }

    public boolean isAvailable(RentalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No se puede verificar disponibilidad: la solicitud de alquiler es nula.");
        }
        if (request.getVehicle() == null) {
            throw new IllegalArgumentException("No se puede verificar disponibilidad: la solicitud no incluye un vehículo.");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("No se puede verificar disponibilidad: la solicitud debe incluir ambas fechas.");
        }
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
