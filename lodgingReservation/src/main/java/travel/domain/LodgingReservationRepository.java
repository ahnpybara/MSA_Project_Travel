package travel.domain;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "lodgingReservations", path = "lodgingReservations")
public interface LodgingReservationRepository
        extends PagingAndSortingRepository<LodgingReservation, Long> {

        Optional<LodgingReservation> findByNameAndReservationDateAndEmailAndRoomcode(String name, Long reservationDate, String email, Long roomCode);

        Optional<LodgingReservation> findById(Long Id);
}