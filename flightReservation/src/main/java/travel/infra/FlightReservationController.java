package travel.infra;

import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import travel.domain.*;
import travel.dto.CancelReservationDTO;
import travel.dto.FlightReservationDTO;


@RestController
public class FlightReservationController {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @Autowired
    FlightReservationService flightReservationService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");    


    @PostMapping("/flightReservations/create") //예약을 만드는 컨트롤러
    public ResponseEntity<?> createFlightReservation(@Valid @RequestBody FlightReservationDTO flightReservationDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                logger.error(error.getDefaultMessage());
            });
            logger.error("\n유효하지 않은 파라미터 입니다.\n");
            return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        }
        try {
            String flightReservationHash = flightReservationService.createHashKey(flightReservationDTO);        // 해쉬값 생성 
            FlightReservation flightReservation = flightReservationService                                      // 중복 검사, 상태 확인 후 생성 메서드 호출.
                                                  .validateAndProcessReservation(flightReservationHash, flightReservationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(flightReservation);
        } 
            catch (NoSuchAlgorithmException e) {
            logger.error("\n예상치 못한 오류로 예약 생성에 실패하였습니다 (해쉬 값 생성 문제일 가능성 높음)\n", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류로 예약 생성에 실패하였습니다.");
        }
    }

    @PostMapping("/flightReservations/cancel") // 예약 취소를 위한 컨트롤러 //TODO 프론트에 long flightReservaionId만 달라고 요청하기.
    public ResponseEntity<?> cancelFlightReservation(@Valid @RequestBody CancelReservationDTO cancelReservationDTO,
            BindingResult bindingResult) {
    
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                logger.error(error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body("유효하지 않은 파라미터 입니다.");
        }
            flightReservationService.cancelFlightReservation(cancelReservationDTO.getFlightReservationId());
            return ResponseEntity.ok("예약 취소 완료");

    }
    
}

