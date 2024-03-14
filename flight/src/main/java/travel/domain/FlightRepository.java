package travel.domain;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "flights", path = "flights")
public interface FlightRepository extends PagingAndSortingRepository<Flight, Long> {

    List<Flight> findByDepAirportAndArrAirportAndDepTimeBetweenAndSeatCapacityGreaterThanEqual(String depAirportNm,
            String arrAirportNm, Long startTimestamp, Long endTimestamp, long minSeatCapacity);
}
