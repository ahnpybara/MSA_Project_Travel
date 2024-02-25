package travel.infra;

import javax.transaction.Transactional;
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
@Transactional
public class PolicyHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentService paymentService;

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRequested'")
    public void wheneverPaymentRequested_ReservtionInfo(@Payload PaymentRequested paymentRequested) {
        paymentService.createPayment(paymentRequested);
        System.out.println("\n\n##### listener ReservtionInfo : " + paymentRequested + "\n\n");
    }

    @Recover
    public void recover(Exception e, PaymentRequested paymentRequested) {
        System.out.println("결제를 처리하기 위한 예약 정보 저장에 실패했습니다.");
    }
}
