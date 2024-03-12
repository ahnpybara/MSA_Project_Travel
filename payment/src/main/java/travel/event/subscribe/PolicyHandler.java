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
import travel.exception.CustomException;
import travel.infra.PaymentService;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentService paymentService;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 결제 요청 이벤트를 수신받아서 결제 정보를 저장하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightReservationRequested' or headers['type']=='LodgingReservationRequested'")
    public void wheneverFlightReservationRequested(@Payload ReservationRequested flightReservationRequested) {
        paymentService.createPayment(flightReservationRequested);
        logger.info("\n\n 예약정보를 수신받았습니다 결제 정보를 저장합니다!\n\n");
    }

    // 결제 취소 요청 이벤트를 수신받아서 결제 정보를 수정하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightReservationCancelRequested' or headers['type']=='LodgingReservationCancelRequested'")
    public void wheneverflightReservationCancelRequested(@Payload ReservationCancelRequested flightReservationCancelRequested) {
        paymentService.updatePayment(flightReservationCancelRequested);
        logger.info("\n\n 예약 취소 요청을 수신받았습니다 결제 상태를 수정합니다!\n\n");
    }

    // 만약 결제 정보를 저장 및 수정하는 도중 예외가 발생했을 때, 재시도가 수행되고 그 재시도 조차 실패할 경우 해당 메서드가 실행
    @Recover
    public void paymentStateRecover(CustomException e, Object paymentInfo) {
        if (paymentInfo instanceof ReservationRequested) {
            logger.error("\n예약 정보를 토대로 결제 정보 저장에 실패했습니다.\n");
        } else if (paymentInfo instanceof ReservationCancelRequested) {
            logger.error("\n해당 예약건에 대한 결제 정보 상태 수정에 실패했습니다\n");
        } else {
            logger.error("\n알 수 없는 오류가 발생하였습니다\n");
        }
    }
}
