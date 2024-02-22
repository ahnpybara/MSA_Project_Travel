package travel.infra;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import travel.domain.*;


//<<< Clean Arch / Inbound Adaptor

@RestController
public class FlightReservationController {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @Autowired
    FlightReservationService flightReservationService;


    @PostMapping("/flightReservations")
    public ResponseEntity<?> createFlightReservation(@Valid @RequestBody FlightReservationDTO flightReservationDTO, BindingResult bindingResult) {
         // 클라이언트에서 전달된 파라미터들이 NULL인지 EMPTY인지 검증을 수행합니다
         if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body("Invalid request parameters");
        } 

        try {
        
            String flightReservationHash = flightReservationService.createHashKey(flightReservationDTO);
            flightReservationService.validateAndProcessReservation(flightReservationHash, flightReservationDTO);
        } catch (ResponseStatusException e) {
            // 예외 발생 시 프론트엔드로부터 받은 메시지와 상태 코드 반환
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }   catch (RollBackException e){
            throw new RuntimeException(e);

        }   catch(NoSuchAlgorithmException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NoSuchAlgorithmException");
        }
        // 모든 검증을 통과한 경우, 생성된 예약 정보 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(flightReservationDTO);
    }

    // 요청하기위해 상태변경 * 임시 추가* 
    @Transactional
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
