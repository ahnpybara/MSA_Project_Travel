package travel.event.subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.exception.RollbackException;
import travel.infra.FlightInfoService;

@Service
public class FlightInfoViewHandler {

    @Autowired
    private FlightInfoService flightInfoService;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 사용자의 예약정보를 저장하는 메서드
    @StreamListener(KafkaProcessor.INPUT)
    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void whenFlightReservationCompleted(@Payload FlightReservationCompleted flightReservationCompleted) {

        if (!flightReservationCompleted.validate()) return;
        logger.info("\n사용자의 예약 정보를 저장합니다.\n");
        flightInfoService.saveFlightInfo(flightReservationCompleted);
    }

    // 사용자의 예약정보 상태를 변경하는 메서드
    @StreamListener(KafkaProcessor.INPUT)
    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void whenFlightReservationRefunded(@Payload FlightReservationRefunded flightReservationRefunded) {

        if (!flightReservationRefunded.validate()) return;
        logger.info("\n사용자의 예약 정보를 수정합니다.\n");
        flightInfoService.updateFlightInfo(flightReservationRefunded);
    }

    // 재시도를 해도 실패했을 경우 실행될 메서드를 정의합니다
    @Recover
    public void flightInfoRecover(RollbackException e, Object flightInfo) {
        logger.error("\n예약된 항공편의 정보를 저장 및 수정하는데 실패했습니다. : " + flightInfo);
        flightInfoService.sendEmail(flightInfo);
    }
}
