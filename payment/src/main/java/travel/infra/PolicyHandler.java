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

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightCancelRequested'")
    public void wheneverFlightCancelRequested_ReservtionInfo(@Payload FlightCancelRequested flightCancelRequested) {
        paymentService.updatePayment(flightCancelRequested);
        System.out.println("\n\n##### listener ReservtionInfo : " + flightCancelRequested + "\n\n");
    }

    @Recover
    public void recover(Exception e, Object paymentInfo) {
        if (paymentInfo instanceof PaymentRequested) {
            System.out.println("예약 정보를 토대로 결제 정보 저장에 실패했습니다.");
        } else if (paymentInfo instanceof FlightCancelRequested) {
            System.out.println("해당 예약건에 대한 결제 정보 상태 수정에 실패했습니다");
        } else {
            System.out.println("알 수 없는 오류가 발생하였습니다 ");
        }
    }
}
