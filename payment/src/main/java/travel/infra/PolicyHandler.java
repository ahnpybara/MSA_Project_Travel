package travel.infra;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentRequested'"
    )
    public void wheneverPaymentRequested_ReservtionInfo(
        @Payload PaymentRequested paymentRequested
    ) {
        PaymentRequested event = paymentRequested;
        System.out.println(
            "\n\n##### listener ReservtionInfo : " + paymentRequested + "\n\n"
        );

        Payment.reservationInfo(event);
    }
}
