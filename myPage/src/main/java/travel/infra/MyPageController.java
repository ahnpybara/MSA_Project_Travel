package travel.infra;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<?> getAndSaveFlightData(@RequestBody FlightInfoDTO request) {
        try {
            if (request == null || request.getUserId() == null) {
                return ResponseEntity.badRequest().body("요청 파라미터가 잘못되었습니다.");
            }

            List<FlightInfo> userFlightInfo = flightInfoRepository.findByUserId(request.getUserId());

            if (userFlightInfo == null || userFlightInfo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자의 항공정보가 없습니다.");
            }

            return ResponseEntity.ok(userFlightInfo);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생하였습니다.");
        }
    }

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/sendMail")
    public String sendMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ahnpybara7627@gmail.com");
        message.setTo("roedeer0807@naver.com");
        message.setSubject("테스트 메일");
        message.setText("테스트 메일 내용입니다.");

        mailSender.send(message);

        return "메일이 성공적으로 보내졌습니다.";
    }
}
