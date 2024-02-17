package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/flightReservations")
@Transactional
public class FlightReservationController {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @PostMapping("/{id}")
    public ResponseEntity<String> reserve(@RequestBody FlightBookCompleted flightBookCompleted){
        System.out.println("예약 진행 메소드");
        FlightReservation flightReservation = new FlightReservation();
        flightReservation.reserve(flightBookCompleted);
        flightReservationRepository.save(flightReservation);
        return ResponseEntity.ok("예약이 완료되었습니다.");
    }
}
//>>> Clean Arch / Inbound Adaptor
