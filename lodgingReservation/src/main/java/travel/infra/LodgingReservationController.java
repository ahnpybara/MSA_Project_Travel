package travel.infra;


import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.*;
import travel.dto.CancelReservationDTO;
import travel.dto.LodgingReservationDTO;

@RestController
@RequestMapping(value = "/lodgingReservations")
public class LodgingReservationController {

    @Autowired
    LodgingReservationRepository lodgingReservationRepository;

    @Autowired
    LodgingReservationService lodgingReservationService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");

    // 예약 생성을 위한 컨트롤러
    @PostMapping("/create")
    public ResponseEntity<?> createLodgingReservation(@Valid @RequestBody LodgingReservationDTO lodgingReservationDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error("\n" + error.getDefaultMessage() + "\n");});
            return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        }    
    }

    // 예약 취소를 위한 컨트롤러
    //TODO 프론트에 long flightReservaionId만 달라고 요청하기.
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelLodgingReservation(@Valid @RequestBody CancelReservationDTO cancelReservationDTO, BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error("\n" + error.getDefaultMessage() + "\n");});
            return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        }
    }
}
