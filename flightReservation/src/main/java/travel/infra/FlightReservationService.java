package travel.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import travel.domain.*;
import travel.domain.FlightReservationRepository;

@Service
public class FlightReservationService {
    
    @Autowired
    private FlightReservationRepository flightReservationRepository;


    public FlightReservation saveFlightReservation(FlightReservation flightReservation){
        return flightReservationRepository.save(flightReservation);
    }
}
