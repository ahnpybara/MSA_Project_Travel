package travel.events.subscribe;


import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;
import travel.infra.FlightReservationEventService;

//<<< Clean Arch / Inbound Adaptor
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

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='Paid'")
    public void wheneverPaid_PaymentComplete(@Payload Paid paid) {

        flightReservationEventService.paymentComplete(paid);
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefunded'")
    public void wheneverPaymentRefunded_PaymentRefund(@Payload PaymentRefunded paymentRefunded) {

        flightReservationEventService.paymentRefund(paymentRefunded);
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentRefundFailed'")
    public void wheneverPaymentRefundFailed_PaymentRefundFail(@Payload PaymentRefundFailed paymentRefundFailed) {

        flightReservationEventService.paymentRefundFail(paymentRefundFailed);
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentCancelled'")
    public void wheneverPaymentCancelled_PaymentCancel(@Payload PaymentCancelled paymentCancelled) {

        flightReservationEventService.paymentCancel(paymentCancelled);
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type']=='PaymentFailed'")
    public void wheneverPaymentFailed_PaymentFailed(@Payload PaymentFailed paymentFailed) {

        flightReservationEventService.paymentFail(paymentFailed);
    }
}
// >>> Clean Arch / Inbound Adaptor
