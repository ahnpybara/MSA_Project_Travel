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
    FlightRepository flightRepository;

    @Autowired
    FlightService flightService;

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRequested'")
    public void wheneverPaymentRequested_ReservtionInfo(@Payload PaymentRequested paymentRequested) {
        flightService.bookSeatCapacity(paymentRequested);
        System.out.println("\n\n##### listener ReservationStatus : " + paymentRequested + "\n\n");
    }

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='FlightbookCancelled'")
    public void wheneverPaymentCnlRequested_ReservationCancellationStatus(
            @Payload FlightbookCancelled flightbookCancelled) {
        flightService.cancelSeatCapacity(flightbookCancelled);
        System.out.println("\n\n##### listener ReservationCancellationStatus : " + flightbookCancelled + "\n\n");
    }

    // TODO 이거 메일 전송이 아닌, SAGA로 하는게 좋을듯
    @Recover
    public void recover(RuntimeException e, Object flightInfo) {
        System.out.println("항공편 좌석 정보 수정에 실패했습니다. 재시도를 중단합니다 : " + flightInfo);
    }
}
