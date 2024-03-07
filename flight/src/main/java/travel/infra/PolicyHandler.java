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
        condition = "headers['type']=='FlightReservaionCancelRequested'"
    )
    public void wheneverFlightReservaionCancelRequested_IncreaseSeatCapcity(
        @Payload FlightReservaionCancelRequested flightReservaionCancelRequested
    ) {
        FlightReservaionCancelRequested event = flightReservaionCancelRequested;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservaionCancelRequested +
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
        condition = "headers['type']=='FlightReservaionCancelled'"
    )
    public void wheneverFlightReservaionCancelled_IncreaseSeatCapcity(
        @Payload FlightReservaionCancelled flightReservaionCancelled
    ) {
        FlightReservaionCancelled event = flightReservaionCancelled;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservaionCancelled +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservaionFailed'"
    )
    public void wheneverFlightReservaionFailed_IncreaseSeatCapcity(
        @Payload FlightReservaionFailed flightReservaionFailed
    ) {
        FlightReservaionFailed event = flightReservaionFailed;
        System.out.println(
            "\n\n##### listener IncreaseSeatCapcity : " +
            flightReservaionFailed +
            "\n\n"
        );

        // Sample Logic //
        Flight.increaseSeatCapcity(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FlightReservaionRequested'"
    )
    public void wheneverFlightReservaionRequested_DecreaseSeatCapcity(
        @Payload FlightReservaionRequested flightReservaionRequested
    ) {
        FlightReservaionRequested event = flightReservaionRequested;
        System.out.println(
            "\n\n##### listener DecreaseSeatCapcity : " +
            flightReservaionRequested +
            "\n\n"
        );

        // Sample Logic //
        Flight.decreaseSeatCapcity(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
