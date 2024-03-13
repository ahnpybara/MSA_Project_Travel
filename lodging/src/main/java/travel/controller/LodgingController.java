package travel.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import travel.domain.Lodging;
import travel.repository.LodgingRepository;
import travel.service.LodgingService;

@RestController
@RequestMapping(value = "/lodgings")
@Transactional
public class LodgingController {

    @Autowired
    LodgingRepository lodgingRepository;

    @Autowired
    LodgingService lodgingService;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 지역별로 숙소 요청을 받아 숙소 리스트를 제공하는 메서드
    @GetMapping("/search")
    public Flux<Lodging> search(@RequestParam(required = true) String areaCode,
            @RequestParam(required = false, defaultValue = "") String sigunguCode,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "6") int numOfRows,
            @RequestParam(required = false, defaultValue = "json") String type) {

        List<Lodging> lodgings = lodgingRepository.findAllByAreaCodeAndSigunguCode(Long.valueOf(areaCode), Long.valueOf(sigunguCode));
        
        if (lodgings.isEmpty()) {
            logger.info("\nNo lodgings found in DB, searching...\n");
            return lodgingService.search(areaCode, sigunguCode, pageNo, numOfRows, type);
        } else {
            logger.info("\nFound lodgings in DB!!!!\n");
            return Flux.fromIterable(lodgings);
        }
    }
}