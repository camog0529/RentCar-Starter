package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;

import java.util.ArrayList;
import java.util.List;

/** Almacenamiento de reservaciones en memoria, según el alcance del parcial. */
public final class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public void save(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Se requiere una reservación.");
        }
        reservations.add(reservation);
    }
}
