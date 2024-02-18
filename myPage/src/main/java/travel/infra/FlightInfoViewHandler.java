package travel.infra;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
public class FlightInfoViewHandler {

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenFlightBookCompleted_then_CREATE_1(@Payload FlightBookCompleted flightBookCompleted) {
        try {
            if (!flightBookCompleted.validate()) return;

            FlightInfo flightInfo = new FlightInfo();
            flightInfo.setId(flightBookCompleted.getId());
            flightInfo.setUserId(flightBookCompleted.getUserId());
            flightInfo.setName(flightBookCompleted.getPassenger());
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
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenFlightbookCancelled_then_UPDATE_1(@Payload FlightbookCancelled flightbookCancelled) {
        try {
            if (!flightbookCancelled.validate()) return;

            List<FlightInfo> flightInfoList = flightInfoRepository.findByUserId(
                    flightbookCancelled.getUserId());
            for (FlightInfo flightInfo : flightInfoList) {
                flightInfo.setStatus(FlightStatus.예약취소);
                flightInfoRepository.save(flightInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
