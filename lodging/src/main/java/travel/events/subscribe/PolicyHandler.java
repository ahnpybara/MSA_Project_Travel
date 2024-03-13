package travel.events.subscribe;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import travel.config.kafka.KafkaProcessor;
import travel.repository.LodgingDetailRepository;
import travel.repository.LodgingIntroRepository;
import travel.repository.LodgingRepository;
import travel.repository.RoomRepository;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    LodgingRepository lodgingRepository;

    @Autowired
    LodgingDetailRepository lodgingDetailRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    LodgingIntroRepository lodgingIntroRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='LodgingReservationRequested'"
    )
    public void wheneverLodgingReservationRequested_IncreaseRoomCapacity(
        @Payload LodgingReservationRequested lodgingReservationRequested
    ) {
        LodgingReservationRequested event = lodgingReservationRequested;
        System.out.println(
            "\n\n##### listener IncreaseRoomCapacity : " +
            lodgingReservationRequested +
            "\n\n"
        );
        // Sample Logic //

    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='LodgingReservationFailed'"
    )
    public void wheneverLodgingReservationFailed_IncreaseRoomCapacity(
        @Payload LodgingReservationFailed lodgingReservationFailed
    ) {
        LodgingReservationFailed event = lodgingReservationFailed;
        System.out.println(
            "\n\n##### listener IncreaseRoomCapacity : " +
            lodgingReservationFailed +
            "\n\n"
        );
        // Sample Logic //

    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='LodgingReservationCancelled'"
    )
    public void wheneverLodgingReservationCancelled_IncreaseRoomCapacity(
        @Payload LodgingReservationCancelled lodgingReservationCancelled
    ) {
        LodgingReservationCancelled event = lodgingReservationCancelled;
        System.out.println(
            "\n\n##### listener IncreaseRoomCapacity : " +
            lodgingReservationCancelled +
            "\n\n"
        );
        // Sample Logic //

    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='LodgingReservationRefunded'"
    )
    public void wheneverLodgingReservationRefunded_IncreaseRoomCapacity(
        @Payload LodgingReservationRefunded lodgingReservationRefunded
    ) {
        LodgingReservationRefunded event = lodgingReservationRefunded;
        System.out.println(
            "\n\n##### listener IncreaseRoomCapacity : " +
            lodgingReservationRefunded +
            "\n\n"
        );
        // Sample Logic //

    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='LodgingReservationCancelRequested'"
    )
    public void wheneverLodgingReservationCancelRequested_DecreaseRoomCapacity(
        @Payload LodgingReservationCancelRequested lodgingReservationCancelRequested
    ) {
        LodgingReservationCancelRequested event =
            lodgingReservationCancelRequested;
        System.out.println(
            "\n\n##### listener DecreaseRoomCapacity : " +
            lodgingReservationCancelRequested +
            "\n\n"
        );
        // Sample Logic //

    }
}
//>>> Clean Arch / Inbound Adaptor
