package travel.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.domain.FlightBookCompleted;
import travel.domain.FlightInfo;
import travel.domain.FlightStatus;
import travel.domain.FlightbookCancelled;

@Service
public class FlightService {

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @Autowired
    private JavaMailSender mailSender;

    // 예약된 항공편 정보를 저장하는 메서드 입니다.
    @Transactional(rollbackFor = Exception.class)
    public void saveFlightInfo(FlightBookCompleted flightBookCompleted) {
        try {
            FlightInfo flightInfo = new FlightInfo();
            flightInfo.setReservationId(flightBookCompleted.getId());
            flightInfo.setUserId(flightBookCompleted.getUserId());
            flightInfo.setName(flightBookCompleted.getName());
            flightInfo.setAirLine(flightBookCompleted.getAirLine());
            flightInfo.setArrAirport(flightBookCompleted.getArrAirport());
            flightInfo.setDepAirport(flightBookCompleted.getDepAirport());
            flightInfo.setArrTime(flightBookCompleted.getArrTime());
            flightInfo.setDepTime(flightBookCompleted.getDepTime());
            flightInfo.setCharge(flightBookCompleted.getCharge());
            flightInfo.setVihicleId(flightBookCompleted.getVihicleId());
            flightInfo.setStatus(FlightStatus.예약완료);
            flightInfoRepository.save(flightInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 특정 예약정보의 상태를 변경하는 메서드입니다
    @Transactional(rollbackFor = Exception.class)
    public void updateFlightInfo(FlightbookCancelled flightbookCancelled) {
        try {
            FlightInfo flightInfo = flightInfoRepository.findByReservationId(flightbookCancelled.getId());
            flightInfo.setStatus(FlightStatus.예약취소);
            flightInfoRepository.save(flightInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 메일을 전송하는 메서드(결제 완료)
    public void sendEmail(Object flightInfo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ahnpybara7627@gmail.com");

        if (flightInfo instanceof FlightBookCompleted) {
            FlightBookCompleted completed = (FlightBookCompleted) flightInfo;
            message.setTo(completed.getEmail());
            message.setSubject("결제 정보를 저장하는 도중 오류가 발생했습니다");
            message.setText("결제 정보를 저장하는 도중 오류가 발생했으니 해당 결제건은 해당 고객센터에 문의 바랍니다.");
        } else if (flightInfo instanceof FlightbookCancelled) {
            FlightbookCancelled cancelled = (FlightbookCancelled) flightInfo;
            message.setTo(cancelled.getEmail());
            message.setSubject("결제 취소하고 해당 정보를 저장하는 도중 오류가 발생했습니다");
            message.setText("결제 취소하고 해당 정보를 저장하는 도중 오류가 발생했으니 해당 결제건은 해당 고객센터에 문의 바랍니다.");
        }

        mailSender.send(message);
        System.out.println("메일이 전송되었습니다");
    }
}