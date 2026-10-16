package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;

import java.util.ArrayList;
import java.util.List;

/** Simple process-local reservation storage, as required by the assignment. */
public final class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public void save(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is required.");
        }
        reservations.add(reservation);
    }
}
