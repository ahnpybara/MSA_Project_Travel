package travel.events.subscribe;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import travel.repository.LodgingDetailRepository;
import travel.repository.LodgingIntroRepository;
import travel.repository.LodgingRepository;
import travel.repository.RoomRepository;

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
}
