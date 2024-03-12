package travel.infra;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.FlightInfo;
import travel.domain.FlightInfoRepository;

@RestController
@RequestMapping(value = "/reservationInfos")
public class MyPageController {

    @Autowired
    FlightInfoRepository flightInfoRepository;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 특정 사용자가 예약한 항공편의 정보를 반환하는 메서드입니다
    @GetMapping("/flight/{userId}")
    public ResponseEntity<?> getAndSaveFlightData(@PathVariable Long userId) {

        try {
            List<FlightInfo> userFlightInfo = flightInfoRepository.findByUserId(userId);
            return ResponseEntity.ok(userFlightInfo);
        } catch (Exception e) {
            logger.error("\n예약된 정보를 조회하던 도중 오류가 발생했습니다 : " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약된 정보를 조회하던 도중 오류가 발생했습니다.");
        }
    }
}
