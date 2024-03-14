package travel.events.subscribe;

import javax.persistence.RollbackException;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import travel.config.kafka.KafkaProcessor;
import travel.repository.LodgingDetailRepository;
import travel.repository.LodgingIntroRepository;
import travel.repository.LodgingRepository;
import travel.repository.RoomRepository;
import travel.service.RoomService;

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
    RoomService roomService;

    @Autowired
    LodgingIntroRepository lodgingIntroRepository;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(
        value = KafkaProcessor.INPUT, condition = "headers['type']=='LodgingReservationRequested'")
    public void wheneverLodgingReservationRequested_IncreaseRoomCapacity(@Payload LodgingReservationRequested lodgingReservationRequested ) {
        logger.info("객실 수를 1 감소합니다.\n");
        roomService.decreaseRoomCapacity(lodgingReservationRequested);
        // Sample Logic //
    }


    
    
    @Retryable(value = RollbackException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @StreamListener(
        value = KafkaProcessor.INPUT, condition = "headers['type']=='LodgingReservationCancelled' or headers['type']=='LodgingReservationFailed' or headers['type']=='LodgingReservationRefunded' or headers['type']=='LodgingReservationCancelRequested'")
    public void wheneverLodgingReservationCancelRequest_IncreaseRoomCapacity(@Payload  LodgingReservationCancelRequested lodgingReservationCancelRequested) {
       logger.info("객실 수 1 증가합니다.");
       roomService.IncreaseRoomCapacity(lodgingReservationCancelRequested);

    }

}
//>>> Clean Arch / Inbound Adaptor
