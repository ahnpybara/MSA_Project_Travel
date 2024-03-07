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
    FlightRepository flightRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationCancelRequested'"
    )
    public void wheneverFlightReservationCancelRequested_IncreaseSeatCapcity(
        @Payload FlightReservationCancelRequested flightReservationCancelRequested
    ) {
        FlightReservationCancelRequested event =
            flightReservationCancelRequested;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservationCancelRequested +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationRefunded'"
    )
    public void wheneverFlightReservationRefunded_IncreaseSeatCapcity(
        @Payload FlightReservationRefunded flightReservationRefunded
    ) {
        FlightReservationRefunded event = flightReservationRefunded;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservationRefunded +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationCancelled'"
    )
    public void wheneverFlightReservationCancelled_IncreaseSeatCapcity(
        @Payload FlightReservationCancelled flightReservationCancelled
    ) {
        FlightReservationCancelled event = flightReservationCancelled;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservationCancelled +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationFailed'"
    )
    public void wheneverFlightReservationFailed_IncreaseSeatCapcity(
        @Payload FlightReservationFailed flightReservationFailed
    ) {
        FlightReservationFailed event = flightReservationFailed;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservationFailed +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservationRequested'"
    )
    public void wheneverFlightReservationRequested_DecreaseSeatCapcity(
        @Payload FlightReservationRequested flightReservationRequested
    ) {
        FlightReservationRequested event = flightReservationRequested;
        System.out.println(
            "\n\n##### listener DecreaseSeatCapcity : " +
            flightReservationRequested +
            "\n\n"
        );

        // Sample Logic //
        Flight.decreaseSeatCapcity(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
