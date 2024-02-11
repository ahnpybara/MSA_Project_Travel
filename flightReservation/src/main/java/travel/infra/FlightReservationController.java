package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import travel.domain.*;


//<<< Clean Arch / Inbound Adaptor

@RestController
@Transactional
public class FlightReservationController {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @Autowired
    FlightReservationService flightReservationService;

    @PostMapping("/flightReservation")
    public ResponseEntity<FlightReservation> createFlightReservation(@RequestBody FlightReservation flightReservation) {
        if(flightReservation == null){              
         return ResponseEntity.badRequest().build();    // 예약을 제대로 못받으면 bad 리턴   
        }
        
        flightReservation.setStatus(Status.예약전);
        FlightReservation savedReservation = flightReservationService.saveFlightReservation(flightReservation);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReservation);
    }
    
}
//>>> Clean Arch / Inbound Adaptor
