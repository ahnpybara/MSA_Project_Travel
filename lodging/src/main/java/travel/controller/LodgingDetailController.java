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
import travel.domain.LodgingDetail;
import travel.repository.LodgingDetailRepository;
import travel.service.LodgingDetailService;

@RestController
@Transactional
public class LodgingDetailController {

    @Autowired
    LodgingDetailRepository lodgingDetailRepository;

    @Autowired
    LodgingDetailService lodgingDetailService;

    private static final Logger log = LoggerFactory.getLogger(LodgingDetailController.class);

    //상세보기 조회
    @GetMapping("/lodgings/searchDetail/{contentid}")
    public Mono<LodgingDetail> searchDeatil(@PathVariable String contentid,
            @RequestParam(required = false, defaultValue = "32") String contenttypeid,
            @RequestParam(required = false, defaultValue = "json") String type) {

                //callable 인터페이스: 값을 반환하는 작업, 쓰레드에 의해 실행
                return Mono.fromCallable(() -> lodgingDetailRepository.findByContentid(Long.valueOf(contentid)))
                .flatMap(optionalLodgingDetail -> {
                    if (optionalLodgingDetail.isPresent()) {
                        log.info("db에 일치하는 데이터 존재");
                        return Mono.just(optionalLodgingDetail.get());
                    } else {
                        log.info("db에 일치하는 데이터가 없음");
                        return lodgingDetailService.searchDetail(contentid, contenttypeid, type);
                    }
                });
    }
    
}