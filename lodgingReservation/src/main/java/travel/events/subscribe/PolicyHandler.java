package travel.events.subscribe;

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
import travel.exception.RollBackException;
import travel.infra.LodgingReservationEventService;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    LodgingReservationRepository lodgingReservationRepository;

    @Autowired
    LodgingReservationEventService lodgingReservationEventService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");

    // TODO Retryable과 Rollback 을 위한 예외 설정 수정 필요합니다 수정이 필요없을수도있음!! 

    // 결제 완료를 수신하는 메서드
    @Retryable(value = RollBackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Paid'")
    public void wheneverPaid_PaymentComplete(@Payload Paid paid) {

        lodgingReservationEventService.paymentComplete(paid);
    }

    // 결제 환불을 수신하는 메서드
    @Retryable(value = RollBackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefunded'")
    public void wheneverPaymentRefunded_PaymentRefund(@Payload PaymentRefunded paymentRefunded) {

        lodgingReservationEventService.paymentRefund(paymentRefunded);
    }

    // 환불 실패를 수신하는 메서드
    @Retryable(value = RollBackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefundFailed'")
    public void wheneverPaymentRefundFailed_PaymentRefundFail(@Payload PaymentRefundFailed paymentRefundFailed) {

        lodgingReservationEventService.paymentRefundFail(paymentRefundFailed);
    }

    // 결제 취소를 수신하는 메서드
    @Retryable(value = RollBackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentCancelled'")
    public void wheneverPaymentCancelled_PaymentCancel(@Payload PaymentCancelled paymentCancelled) {

        lodgingReservationEventService.paymentCancel(paymentCancelled);
    }

    // 결제 실패를 수신하는 메서드
    @Retryable(value = RollBackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentFailed'")
    public void wheneverPaymentFailed_PaymentFailed(@Payload PaymentFailed paymentFailed) {

        lodgingReservationEventService.paymentFail(paymentFailed);
    }

    // TODO 리커버 메서드 추가!!
        // 실패에 대한 retry후 recoverAfterRetryFailure 복구메서드 실행.
    @Recover
    public void recoverAfterRetryFailure(RollBackException e, Object eventInfo) {
        if (eventInfo instanceof Paid) {
            logger.error("\nPaid event 복구 메서드 실행\n" + e.getMessage());
        } else if (eventInfo instanceof PaymentRefunded) {
            logger.error("\nPaymentRefunded event 복구 메서드 실행\n" + e.getMessage());
        } else if (eventInfo instanceof PaymentRefundFailed) {
            logger.error("\nPaymentRefundFailed event 복구 메서드 실행\n" + e.getMessage());
        } else if (eventInfo instanceof PaymentCancelled) {
            logger.error("\nPaymentCancelled event 복구 메서드 실행\n" + e.getMessage());
        } else if (eventInfo instanceof PaymentFailed) {
            logger.error("\nPaymentFailed event 복구 메서드 실행\n" + e.getMessage());
        } else {
            logger.error("unknown event 복구 메서드 실행 " + e.getMessage());
        }
    }
}
