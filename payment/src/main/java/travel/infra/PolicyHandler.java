package travel.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentCnlRequested'"
    )
    public void wheneverPaymentCnlRequested_ReservtionCancelInfo(
        @Payload PaymentCnlRequested paymentCnlRequested
    ) {
        PaymentCnlRequested event = paymentCnlRequested;
        System.out.println(
            "\n\n##### listener ReservtionCancelInfo : " +
            paymentCnlRequested +
            "\n\n"
        );

        // Sample Logic //
        Payment.reservtionCancelInfo(event);
    }

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

        // Sample Logic //
        Payment.reservtionInfo(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
