package travel.controller;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 숙소 인트로 소개
    @GetMapping("/lodgings/searchRoom/{contentid}")
    public Flux<Room> searchDeatil(@PathVariable String contentid,
            @RequestParam(required = false, defaultValue = "32") String contenttypeid,
            @RequestParam(required = false, defaultValue = "json") String type) {

        return Mono.fromCallable(() -> roomRepository.findByContentid(Long.valueOf(contentid)))
                .flatMapMany(rooms -> {
                    if (!rooms.isEmpty()) {
                        logger.info("db에 일치하는 데이터 존재");
                        return Flux.fromIterable(rooms);
                    } else {
                        logger.info("db에 일치하는 데이터가 없음");
                        return roomService.searchRoom(contentid, contenttypeid, type);
                    }
                });
    }

    @GetMapping("/rooms/roomCapacity")
    public ResponseEntity<Long> getRoomCapacity(@RequestParam("roomCode") Long roomCode,
            @RequestParam("reservationDate") Long reservationDate) {
        Long roomCapacity = roomService.getRoomCapacityByDate(roomCode, reservationDate);
        logger.info(roomCapacity + " 해당 숙소의 남은 방은");
        if (roomCapacity == -1L) {
            logger.error("\n해당 날짜에 대한 방을 찾을수 없습니다.\n");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(roomCapacity, HttpStatus.OK);
    }
}
// >>> Clean Arch / Inbound Adaptor
