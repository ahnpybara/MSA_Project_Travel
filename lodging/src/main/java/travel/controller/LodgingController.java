package travel.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import travel.domain.Lodging;
import travel.repository.LodgingRepository;
import travel.service.LodgingService;

@RestController
@Transactional
public class LodgingController {

    @Autowired
    LodgingRepository lodgingRepository;

    @Autowired
    LodgingService lodgingService;

    private static final Logger log = LoggerFactory.getLogger(LodgingController.class);

    @GetMapping("/lodgings/search")
    public Flux<Lodging> search(@RequestParam(required = true) String areaCode,
            @RequestParam(required = false, defaultValue = "") String sigunguCode,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "12") int numOfRows,
            @RequestParam(required = false, defaultValue = "json") String type) {

                
        List<Lodging> lodgings = lodgingRepository.findAllByAreaCode(Long.valueOf(areaCode));
        Flux<Lodging> lodgingFlux = Flux.fromIterable(lodgings);
        if (lodgings.isEmpty()) {
            log.info("No lodgings found in DB, searching...");
            lodgingFlux = lodgingService.search(areaCode, sigunguCode, pageNo, numOfRows, type);
        } else {
            log.info("Found lodgings in DB!!!!");
        }
        return lodgingFlux;
    }
}