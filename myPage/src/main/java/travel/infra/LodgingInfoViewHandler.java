package travel.infra;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
public class LodgingInfoViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private LodgingInfoRepository lodgingInfoRepository;
    //>>> DDD / CQRS
}
