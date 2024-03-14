package travel.infra;


import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import travel.domain.LodgingReservation;
import travel.domain.LodgingReservationRepository;
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

        // if (bindingResult.hasErrors()) {
        //     bindingResult.getAllErrors().forEach(error -> {logger.error("\n" + error.getDefaultMessage() + "\n");});
        //     return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        // }

        LodgingReservation lodgingReservation = lodgingReservationService.validateAndProcessReservation(lodgingReservationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(lodgingReservation);
     
    }

    // 예약 취소를 위한 컨트롤러
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelLodgingReservation(@Valid @RequestBody CancelReservationDTO cancelReservationDTO, BindingResult bindingResult) {
        
        // if (bindingResult.hasErrors()) {
        //     bindingResult.getAllErrors().forEach(error -> {logger.error("\n" + error.getDefaultMessage() + "\n");});
        //     return ResponseEntity.badRequest().body("\n유효하지 않은 파라미터 입니다.");
        // }

        lodgingReservationService.cancelLodgingReservation(cancelReservationDTO.getLodgingReservationId());
        return ResponseEntity.ok("예약 취소 완료");
    }
}
