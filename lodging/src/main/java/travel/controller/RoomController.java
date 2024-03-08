package travel.controller;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import travel.domain.Room;
import travel.repository.RoomRepository;
import travel.service.RoomService;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/rooms")
@Transactional
public class RoomController {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoomService roomService;

    private final static Logger logger = LoggerFactory.getLogger(Room.class);

      //숙소 인트로 소개
    @GetMapping("/lodgings/searchRoom/{contentid}")
    public Flux<Room> searchDeatil(@PathVariable String contentid,
            @RequestParam(required = false, defaultValue = "32") String contenttypeid,
            @RequestParam(required = false, defaultValue = "json") String type) {

        // callable 인터페이스: 값을 반환하는 작업, 쓰레드에 의해 실행
        return Mono.fromCallable(() -> roomRepository.findByContentid(Long.valueOf(contentid)))
                .flatMapMany(optionalRoom -> {
                    if (optionalRoom.isPresent()) {
                        logger.info("db에 일치하는 데이터 존재");
                        return Flux.just(optionalRoom.get());
                    } else {
                        logger.info("db에 일치하는 데이터가 없음");
                        return roomService.searchRoom(contentid, contenttypeid, type);
                    }
                });
    }
}
//>>> Clean Arch / Inbound Adaptor
