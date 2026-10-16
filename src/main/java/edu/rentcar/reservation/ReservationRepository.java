package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;

import java.util.List;

/** Contrato para almacenar reservaciones en memoria durante la ejecución. */
public interface ReservationRepository {
    List<Reservation> findAll();

    void save(Reservation reservation);
}
