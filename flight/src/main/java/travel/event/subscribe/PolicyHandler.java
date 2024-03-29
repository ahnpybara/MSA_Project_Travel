package travel.event.subscribe;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;
import travel.exception.RollbackException;
import travel.infra.FlightService;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    FlightService flightService;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 항공편 취소 환불 실패 이벤트를 수신받아 해당 항공편의 좌석수를 1 증가하는 메서드입니다 
    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightReservationCancelled' or headers['type']=='FlightReservationCancelRequested' or headers['type']=='FlightReservationFailed' or headers['type']=='FlightReservationRefunded'")
    public void wheneverFlightReservationCancelled(@Payload FlightReservationCancelled flightReservationCancelled) {
        logger.info("\n" + flightReservationCancelled.getFlightId() + "번 항공편의 좌석수를 1 증가합니다.\n");
        flightService.increaseSeatCapacity(flightReservationCancelled);
    }

    // 항공편 예약 이벤트를 수신받아 해당 항공편의 좌석수를 1 감소하는 메서드입니다 
    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightReservationRequested'")
    public void wheneverFlightReservationRequested(@Payload FlightReservationRequested flightReservationRequested) {
        logger.info("\n" + flightReservationRequested.getFlightId() + "번 항공편의 좌석수를 1 감소합니다.\n");
        flightService.decreaseSeatCapacity(flightReservationRequested);
    }

    // TODO 이걸 어떻게 SAGA로 해야햘까..? -> 일단 재시도로 처리
    @Recover
    public void recover(RollbackException e, Object flightInfo) {
        logger.error("\n항공편의 좌석 정보 수정에 실패했습니다.\n");
    }
}
