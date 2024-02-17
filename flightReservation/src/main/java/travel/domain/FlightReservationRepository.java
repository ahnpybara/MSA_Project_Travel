package travel.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "flightReservations",
    path = "flightReservations"
)
public interface FlightReservationRepository
    extends PagingAndSortingRepository<FlightReservation, Long> {}