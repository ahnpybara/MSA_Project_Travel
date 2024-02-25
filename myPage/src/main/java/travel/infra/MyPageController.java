package travel.infra;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import travel.domain.FlightInfo;
import travel.domain.FlightInfoDTO;

@RestController
@Transactional
public class MyPageController {

    @Autowired
    FlightInfoRepository flightInfoRepository;

    @PostMapping("/flightInfos")
    public ResponseEntity<?> getAndSaveFlightData(@Valid @RequestBody FlightInfoDTO request, BindingResult bindingResult) {
        
        // 클라이언트에서 전달된 파라미터들이 NULL인지 EMPTY인지 검증을 수행합니다
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {System.out.println(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("유저 ID가 존재하지 않습니다 (잘못된 파라미터)");
        }

        try {
            List<FlightInfo> userFlightInfo = flightInfoRepository.findByUserId(request.getUserId());
            return ResponseEntity.ok(userFlightInfo);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생하였습니다.");
        }
    }
}
