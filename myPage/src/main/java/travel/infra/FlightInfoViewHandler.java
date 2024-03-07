package travel.infra;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
public class FlightInfoViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenFlightReservationCompleted_then_CREATE_1(
        @Payload FlightReservationCompleted flightReservationCompleted
    ) {
        try {
            if (!flightReservationCompleted.validate()) return;

            // view 객체 생성
            FlightInfo flightInfo = new FlightInfo();
            // view 객체에 이벤트의 Value 를 set 함
            flightInfo.setId(flightReservationCompleted.getId());
            flightInfo.setUserId(flightReservationCompleted.getUserId());
            flightInfo.setName(flightReservationCompleted.getPassenger());
            flightInfo.setAirLine(flightReservationCompleted.getAirLine());
            flightInfo.setArrAirport(
                flightReservationCompleted.getArrAirport()
            );
            flightInfo.setDepAirport(
                flightReservationCompleted.getDepAirport()
            );
            flightInfo.setArrTime(flightReservationCompleted.getArrTime());
            flightInfo.setDepTime(flightReservationCompleted.getDepTime());
            flightInfo.setCharge(flightReservationCompleted.getCharge());
            flightInfo.setVihicleId(flightReservationCompleted.getVihicleId());
            flightInfo.setStatus("예약완료");
            // view 레파지 토리에 save
            flightInfoRepository.save(flightInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenFlightReservationRefunded_then_UPDATE_1(
        @Payload FlightReservationRefunded flightReservationRefunded
    ) {
        try {
            if (!flightReservationRefunded.validate()) return;
            // view 객체 조회

            List<FlightInfo> flightInfoList = flightInfoRepository.findByUserId(
                flightReservationRefunded.getUserId()
            );
            for (FlightInfo flightInfo : flightInfoList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                flightInfo.setStatus("예약취소");
                // view 레파지 토리에 save
                flightInfoRepository.save(flightInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
