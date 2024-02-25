package travel.domain;


import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;




//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "flightReservations",
    path = "flightReservations"
)
public interface FlightReservationRepository
    extends PagingAndSortingRepository<FlightReservation, Long> {

        Optional<FlightReservation> findByReservationHash(String id);
    }


