package travel.events.subscribe;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;
import travel.exception.CustomException;
import travel.infra.FlightReservationEventService;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    FlightReservationRepository flightReservationRepository;

    @Autowired
    FlightReservationEventService flightReservationEventService;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
    }

    // 결제 완료를 수신하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Paid'")
    public void wheneverPaid_PaymentComplete(@Payload Paid paid) {

        flightReservationEventService.paymentComplete(paid);
    }

    // 결제 환불을 수신하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefunded'")
    public void wheneverPaymentRefunded_PaymentRefund(@Payload PaymentRefunded paymentRefunded) {

        flightReservationEventService.paymentRefund(paymentRefunded);
    }

    // 환불 실패를 수신하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefundFailed'")
    public void wheneverPaymentRefundFailed_PaymentRefundFail(@Payload PaymentRefundFailed paymentRefundFailed) {

        flightReservationEventService.paymentRefundFail(paymentRefundFailed);
    }

    // 결제 취소를 수신하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentCancelled'")
    public void wheneverPaymentCancelled_PaymentCancel(@Payload PaymentCancelled paymentCancelled) {

        flightReservationEventService.paymentCancel(paymentCancelled);
    }

    // 결제 실패를 수신하는 메서드
    @Retryable(value = CustomException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentFailed'")
    public void wheneverPaymentFailed_PaymentFailed(@Payload PaymentFailed paymentFailed) {

        flightReservationEventService.paymentFail(paymentFailed);
    }
}
// >>> Clean Arch / Inbound Adaptor
