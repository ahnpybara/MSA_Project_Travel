package travel.infra;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import travel.domain.*;
import travel.dto.FlightReservationDTO;


@RestController
public class FlightReservationController {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @Autowired
    FlightReservationService flightReservationService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");    


    @PostMapping("/flightReservations") //예약을 만드는 컨트롤러
    public ResponseEntity<?> createFlightReservation(@Valid @RequestBody FlightReservationDTO flightReservationDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                logger.error(error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        }
        try {
            String flightReservationHash = flightReservationService.createHashKey(flightReservationDTO);
            FlightReservation flightReservation = flightReservationService
                    .validateAndProcessReservation(flightReservationHash, flightReservationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(flightReservation);
        } catch (Exception e) {
            logger.error("\n예상치 못한 오류로 예약 생성에 실패하였습니다 (해쉬 값 생성 문제일 가능성 높음)", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류로 예약 생성에 실패하였습니다.");
        }
    }

    @PostMapping("/flightReservations/cancel")
    public ResponseEntity<?> cancelFlightReservation(@Valid @RequestBody FlightReservationDTO flightReservationDTO,
            BindingResult bindingResult) {
    
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                logger.error(error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        }
            flightReservationService.cancelFlightReservation(flightReservationDTO.getId());
            return ResponseEntity.ok("예약 취소 완료");

    }
    
}

