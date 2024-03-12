package travel.domain;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "flightReservations", path = "flightReservations")
// TODO Optional -> null 허용하는 것이라 굳이 안써도됨.
public interface FlightReservationRepository
        extends PagingAndSortingRepository<FlightReservation, Long> {

    Optional<FlightReservation> findByReservationHash(String id);

    Optional<FlightReservation> findById(Long Id);

}
