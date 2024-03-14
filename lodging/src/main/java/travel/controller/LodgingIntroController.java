package travel.controller;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import travel.domain.LodgingIntro;
import travel.repository.LodgingIntroRepository;
import travel.service.LodgingIntroService;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/lodgingIntros")
@Transactional
public class LodgingIntroController {

    @Autowired
    LodgingIntroService lodgingIntroService;

    @Autowired
    LodgingIntroRepository lodgingIntroRepository;

    private final static Logger logger = LoggerFactory.getLogger(LodgingIntro.class);

    // 숙소 인트로 소개
    @GetMapping("/lodgings/searchIntro/{contentid}")
    public Mono<LodgingIntro> searchDeatil(@PathVariable String contentid,
            @RequestParam(required = false, defaultValue = "32") String contenttypeid,
            @RequestParam(required = false, defaultValue = "json") String type) {

        // callable 인터페이스: 값을 반환하는 작업, 쓰레드에 의해 실행
        return Mono.fromCallable(() -> lodgingIntroRepository.findByContentid(Long.valueOf(contentid)))
                .flatMap(optionalLodgingIntro -> {
                    if (optionalLodgingIntro.isPresent()) {
                        logger.info("db에 일치하는 데이터 존재");
                        return Mono.just(optionalLodgingIntro.get());
                    } else {
                        logger.info("db에 일치하는 데이터가 없음");
                        return lodgingIntroService.searchIntro(contentid, contenttypeid, type);
                    }
                });
    }
}
// >>> Clean Arch / Inbound Adaptor
