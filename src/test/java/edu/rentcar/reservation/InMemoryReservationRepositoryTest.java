package edu.rentcar.reservation;

import edu.rentcar.model.Reservation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryReservationRepositoryTest {

    @Test
    void savesReservationAndReturnsItFromFindAll() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation = new Reservation("RES-1", "CUS-1", "CAR-1",
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), "CONFIRMED");

        repository.save(reservation);

        assertEquals(java.util.List.of(reservation), repository.findAll());
    }

    @Test
    void returnedReservationListCannotModifyRepositoryContents() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        assertThrows(UnsupportedOperationException.class, () -> repository.findAll().clear());
    }

    @Test
    void refusesToSaveNullReservation() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertTrue(repository.findAll().isEmpty());
    }
}
