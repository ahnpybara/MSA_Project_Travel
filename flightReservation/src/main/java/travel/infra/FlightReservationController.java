package travel.infra;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> createFlightReservation(@RequestBody FlightReservation flightReservation) throws NoSuchAlgorithmException {
        if (flightReservation == null) {              
            return ResponseEntity.badRequest().body("Invalid reservation data.");
        }
        
        String flightReservationHash = flightReservationService.createHashKey(flightReservation);
    
        try {
            flightReservationService.validateAndProcessReservation(flightReservationHash, flightReservation);
            // 이후 로직은 validateAndProcessReservation 메서드 내에서 예외 발생 시 중단됩니다.
        } catch (ResponseStatusException e) {
            // 예외 발생 시 프론트엔드로부터 받은 메시지와 상태 코드 반환
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    
        // 모든 검증을 통과한 경우, 생성된 예약 정보 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(flightReservation);
    }
    // 요청하기위해 상태변경 * 임시 추가* 
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateReservationStatus(@PathVariable Long id, @RequestBody Status status) {
        try {
            FlightReservation updatedReservation = flightReservationService.updateReservationStatus(id, status);
            return ResponseEntity.ok(updatedReservation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found or update failed");
        }
    }
}
//>>> Clean Arch / Inbound Adaptor
