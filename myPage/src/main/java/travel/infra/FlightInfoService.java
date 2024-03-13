package travel.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import travel.domain.FlightInfo;
import travel.domain.FlightInfoRepository;
import travel.domain.FlightStatus;
import travel.event.subscribe.FlightReservationCompleted;
import travel.event.subscribe.FlightReservationRefunded;
import travel.exception.RollbackException;

@Service
public class FlightInfoService {

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 예약된 항공편 정보를 저장하는 메서드 입니다.
    @Transactional(rollbackFor = RollbackException.class)
    public void saveFlightInfo(FlightReservationCompleted flightReservationCompleted) {

        try {
            FlightInfo exsistFlight = flightInfoRepository.findByReservationId(flightReservationCompleted.getId());

            if (exsistFlight == null) {
                FlightInfo flightInfo = new FlightInfo();
                flightInfo.setReservationId(flightReservationCompleted.getId());
                flightInfo.setUserId(flightReservationCompleted.getUserId());
                flightInfo.setAirLine(flightReservationCompleted.getAirLine());
                flightInfo.setArrAirport(flightReservationCompleted.getArrAirport());
                flightInfo.setDepAirport(flightReservationCompleted.getDepAirport());
                flightInfo.setArrTime(flightReservationCompleted.getArrTime());
                flightInfo.setDepTime(flightReservationCompleted.getDepTime());
                flightInfo.setCharge(flightReservationCompleted.getCharge());
                flightInfo.setVihicleId(flightReservationCompleted.getVihicleId());
                flightInfo.setUserId(flightReservationCompleted.getUserId());
                flightInfo.setName(flightReservationCompleted.getName());
                flightInfo.setEmail(flightReservationCompleted.getEmail());
                flightInfo.setCategory(flightReservationCompleted.getCategory());
                flightInfo.setStatus(FlightStatus.예약완료);
                flightInfoRepository.save(flightInfo);
            } else {
                logger.info("\n"+ flightReservationCompleted.getId() + "번 예약 현황이 이미 존재하므로 상태만 예약 완료로 변경됩니다\n");
                exsistFlight.setStatus(FlightStatus.예약완료);
                flightInfoRepository.save(exsistFlight);
            }
        } catch (Exception e) {
            logger.error("\n예약된 정보를 저장하는 도중 문제가 발생했습니다 : " + e);
            throw new RollbackException("예약된 정보를 저장하는 도중 문제가 발생했습니다 : " + e);
        }
    }

    // 특정 예약정보의 상태를 변경하는 메서드입니다
    @Transactional(rollbackFor = RollbackException.class)
    public void updateFlightInfo(FlightReservationRefunded flightReservationRefunded) {

        try {
            FlightInfo flightInfo = flightInfoRepository.findByReservationId(flightReservationRefunded.getId());

            if (flightInfo == null) {
                logger.error("\n" + flightReservationRefunded.getId() + "번 예약 번호로 예약된 항공 정보가 존재하지 않습니다\n");
            } else {
                flightInfo.setStatus(FlightStatus.예약취소);
                flightInfoRepository.save(flightInfo);
            }
        } catch (Exception e) {
            logger.error("\n예약된 정보를 수정하는 도중 문제가 발생했습니다 : " + e);
            throw new RollbackException("예약된 정보를 수정하는 도중 문제가 발생했습니다 : " + e);
        }
    }

    // 만약 예약정보를 수정 및 저장하는 도중 문제 발생시 이를 메일로 전송하는 메서드
    public void sendEmail(Object flightInfo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ahnpybara7627@gmail.com");

        if (flightInfo instanceof FlightReservationCompleted) {
            FlightReservationCompleted completed = (FlightReservationCompleted) flightInfo;
            message.setTo(completed.getEmail());
            message.setSubject("결제 정보를 저장하는 도중 오류가 발생했습니다");
            message.setText("결제 정보를 저장하는 도중 오류가 발생했으니 해당 결제건은 해당 고객센터에 문의 바랍니다.");
        } else if (flightInfo instanceof FlightReservationRefunded) {
            FlightReservationRefunded cancelled = (FlightReservationRefunded) flightInfo;
            message.setTo(cancelled.getEmail());
            message.setSubject("결제 취소하고 해당 정보를 저장하는 도중 오류가 발생했습니다");
            message.setText("결제 취소하고 해당 정보를 저장하는 도중 오류가 발생했으니 해당 결제건은 해당 고객센터에 문의 바랍니다.");
        }
        mailSender.send(message);
        logger.info("\n메일이 전송되었습니다\n");
    }
}