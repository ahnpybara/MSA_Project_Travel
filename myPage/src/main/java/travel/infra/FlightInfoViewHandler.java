package travel.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
public class FlightInfoViewHandler {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightInfoRepository flightInfoRepository;

    // 예약된 항공편의 정보를 저장하는 메서드
    @StreamListener(KafkaProcessor.INPUT)
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void whenFlightBookCompleted_then_CREATE_1(@Payload FlightBookCompleted flightBookCompleted) {

        FlightInfo flightInfo = flightInfoRepository.findByReservationId(flightBookCompleted.getId());
        if (!flightBookCompleted.validate() || flightInfo != null) return;
        System.out.println("예약된 항공편의 정보를 저장합니다");
        flightService.saveFlightInfo(flightBookCompleted);
    }

    // 예약된 항공편의 상태를 변경하는 메서드
    @StreamListener(KafkaProcessor.INPUT)
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void whenFlightbookCancelled_then_UPDATE_1(@Payload FlightbookCancelled flightbookCancelled) {

        if (!flightbookCancelled.validate())
            return;
        System.out.println("예약된 항공편의 정보를 변경합니다");
        flightService.updateFlightInfo(flightbookCancelled);
    }

    // 재시도를 3번 시도 했지만 3번다 실패시 실행될 메서드를 정의합니다
    @Recover
    public void recover(RuntimeException e, Object flightInfo) {
        System.out.println("예약된 항공편의 정보를 저장 및 수정하는데 실패했습니다. : " + flightInfo);
        flightService.sendEmail(flightInfo);
    }
}
