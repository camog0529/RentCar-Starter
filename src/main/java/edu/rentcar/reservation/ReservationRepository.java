package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;

import java.util.List;

/** In-memory reservation storage contract for the current application scope. */
public interface ReservationRepository {
    List<Reservation> findAll();

    void save(Reservation reservation);
}
