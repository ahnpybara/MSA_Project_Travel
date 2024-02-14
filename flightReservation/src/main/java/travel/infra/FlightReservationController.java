package travel.infra;

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

        
        flightReservation.setStatus(Status.결제대기);
        FlightReservation savedReservation = flightReservationService.saveFlightReservation(flightReservation);      
        
        flightReservationService.scheduleReservationTimeoutCheck(savedReservation.getId());

     return ResponseEntity.status(HttpStatus.CREATED).body(savedReservation);  

}
}
//>>> Clean Arch / Inbound Adaptor
