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
    FlightReservationRepository flightReservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='Paid'"
    )
    public void wheneverPaid_PaymentComplete(@Payload Paid paid) {
        Paid event = paid;
        System.out.println(
            "\n\n##### listener PaymentComplete : " + paid + "\n\n"
        );

        // Sample Logic //
        FlightReservation.paymentComplete(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentRefunded'"
    )
    public void wheneverPaymentRefunded_PaymentRefund(
        @Payload PaymentRefunded paymentRefunded
    ) {
        PaymentRefunded event = paymentRefunded;
        System.out.println(
            "\n\n##### listener PaymentRefund : " + paymentRefunded + "\n\n"
        );

        // Sample Logic //
        FlightReservation.paymentRefund(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentRefundFailed'"
    )
    public void wheneverPaymentRefundFailed_PaymentRefundFail(
        @Payload PaymentRefundFailed paymentRefundFailed
    ) {
        PaymentRefundFailed event = paymentRefundFailed;
        System.out.println(
            "\n\n##### listener PaymentRefundFail : " +
            paymentRefundFailed +
            "\n\n"
        );

        // Sample Logic //
        FlightReservation.paymentRefundFail(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentCancelled'"
    )
    public void wheneverPaymentCancelled_PaymentCancel(
        @Payload PaymentCancelled paymentCancelled
    ) {
        PaymentCancelled event = paymentCancelled;
        System.out.println(
            "\n\n##### listener PaymentCancel : " + paymentCancelled + "\n\n"
        );

        // Sample Logic //
        FlightReservation.paymentCancel(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentFailed'"
    )
    public void wheneverPaymentFailed_PaymentFailed(
        @Payload PaymentFailed paymentFailed
    ) {
        PaymentFailed event = paymentFailed;
        System.out.println(
            "\n\n##### listener PaymentFailed : " + paymentFailed + "\n\n"
        );

        // Sample Logic //
        FlightReservation.paymentFailed(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
