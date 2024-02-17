package travel.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "flights", path = "flights")
public interface FlightRepository
    extends PagingAndSortingRepository<Flight, Long> {

    Object findByDepAirportAndArrAirportAndDepTimeBetween(String depAirportNm, String arrAirportNm, Long startTimestamp,
            Long endTimestamp);}
