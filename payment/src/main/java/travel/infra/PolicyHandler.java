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
        condition = "headers['type']=='FlightReservationRequested'"
    )
    public void wheneverFlightReservationRequested_ReceiveReservationInfo(
        @Payload FlightReservationRequested flightReservationRequested
    ) {
        FlightReservationRequested event = flightReservationRequested;
        System.out.println(
            "\n\n##### listener ReceiveReservationInfo : " +
            flightReservationRequested +
            "\n\n"
        );

        // Sample Logic //
        Payment.receiveReservationInfo(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationCancelRequested'"
    )
    public void wheneverFlightReservationCancelRequested_ReceiveReservationCancelRequest(
        @Payload FlightReservationCancelRequested flightReservationCancelRequested
    ) {
        FlightReservationCancelRequested event =
            flightReservationCancelRequested;
        System.out.println(
            "\n\n##### listener ReceiveReservationCancelRequest : " +
            flightReservationCancelRequested +
            "\n\n"
        );

        // Sample Logic //
        Payment.receiveReservationCancelRequest(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
