package travel.infra;

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
}
//>>> Clean Arch / Inbound Adaptor
