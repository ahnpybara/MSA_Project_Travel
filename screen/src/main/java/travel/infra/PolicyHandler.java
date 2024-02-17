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
    FlightRepository flightRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentRequested'"
    )
    public void wheneverPaymentRequested_ReservationStatus(
        @Payload PaymentRequested paymentRequested
    ) {
        PaymentRequested event = paymentRequested;
        System.out.println(
            "\n\n##### listener ReservationStatus : " +
            paymentRequested +
            "\n\n"
        );

        Flight.reservationStatus(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='PaymentCnlRequested'"
    )
    public void wheneverPaymentCnlRequested_ReservationCancellationStatus(
        @Payload PaymentCnlRequested paymentCnlRequested
    ) {
        PaymentCnlRequested event = paymentCnlRequested;
        System.out.println(
            "\n\n##### listener ReservationCancellationStatus : " +
            paymentCnlRequested +
            "\n\n"
        );

        Flight.reservationCancellationStatus(event);
    }
}
