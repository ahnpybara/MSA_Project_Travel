package travel.service;

import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.LodgingDetail;
import travel.exception.CustomException;
import travel.exception.RetryExhaustedException;
import travel.repository.LodgingDetailRepository;

@Service
@RequiredArgsConstructor
// 숙박 정보를 책임지는 서비스 (전체 숙소 정보 조회, 숙소 상세 정보는 숙박 정보를 책임지기에 단일 원칙 책임에 적합)
public class LodgingDetailService {

    private final WebClient webClient;

    private final LodgingDetailRepository lodgingDetailRepository;

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(LodgingDetailService.class);

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.mobileOS}")
    private String mobileOS;

    @Value("${api.mobileApp}")
    private String mobileApp;

    @Value("${api.arrange}")
    private String arrange;

    public Mono<LodgingDetail> searchDetail(String contentId, String contentTypeId, String type) {
        String defaultYN = "Y";
        String firstImageYN = "Y";
        String areacodeYN = "Y";
        String catcodeYN = "Y";
        String addrinfoYN = "Y";
        String mapinfoYN = "Y";
        String overviewYN = "Y";

        String urlDetailLodging = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailCommon1?ServiceKey=%s&contentTypeId=%s&contentId=%s&MobileOS=%s&MobileApp=%s&defaultYN=%s&firstImageYN=%s&areacodeYN=%s&catcodeYN=%s&addrinfoYN=%s&mapinfoYN=%s&overviewYN=%s&_type=%s",
                serviceKey, contentTypeId, contentId, mobileOS, mobileApp, defaultYN, firstImageYN, areacodeYN,
                catcodeYN, addrinfoYN, mapinfoYN, overviewYN, type);
        String urlForDetailIntro = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailIntro1?ServiceKey=%s&contentTypeId=%s&contentId=%s&MobileOS=%s&MobileApp=%s&_type=%s",
                serviceKey, contentTypeId, contentId, mobileOS, mobileApp, type);

        Mono<JsonNode> detailLodging = webClient.get()
                .uri(URI.create(urlDetailLodging))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                // .retryWhen(createRetrySpec())
                .flatMap(jsonNode -> {
                    JsonNode itemNode = jsonNode.path("response").path("body").path("items").path("item");
                    if (itemNode.isArray() && itemNode.size() > 0) {
                        return Mono.justOrEmpty(itemNode.get(0));
                    } else {
                        logger.info("detail 빈 객체 반환");
                        return Mono.empty();
                    }
                });

        // get(0)을 호출하면, 시스템은 예외를 발생시킵니다. 이는 리스트가 비어있음을 즉시 알아챌 수 있게 해주므로, 에러를 빠르게 찾고 수정할
        // 수 있습니다.

        Mono<JsonNode> detailIntro = webClient.get()
                .uri(URI.create(urlForDetailIntro))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                // .retryWhen(createRetrySpec())

                .flatMap(jsonNode -> {
                    JsonNode itemNode = jsonNode.path("response").path("body").path("items").path("item");
                    if (itemNode.isArray() && itemNode.size() > 0) {
                        return Mono.justOrEmpty(itemNode.get(0));
                    } else {
                        logger.info("intro 빈 객체 반환");
                        return Mono.empty();
                    }
                });

        return Mono.zip(detailLodging, detailIntro)
                .flatMap(tuple -> {
                    JsonNode detailNode = tuple.getT1();
                    JsonNode introNode = tuple.getT2();

                    // 두 결과를 하나로 합칩니다. null이 아닌 속성을 선택합니다.
                    // objectNode, jsonNode의 하위 객체로 jsonNode는 json트리의 노드를 나타내면, objectNode는 json객체를
                    // 나타냄
                    ObjectNode mergedNode = (ObjectNode) detailNode.deepCopy(); // 데이터의 구조를 완전히 복사하여 새로운 객체 생성
                    introNode.fields().forEachRemaining(field -> {// Iterator 인터페이스의 forEachRemaining 메소드를 사용하여
                                                                  // introNode의 모든 필드를 순회하는 코드

                        // Iterator는 JSON 객체의 각 필드를 차례대로 방문할 수 있게 해주는 도구
                        if (field.getValue() != null && !field.getValue().isNull()) {// 메모리에 할당되어 있으며, 그 값이 null 아닌 것
                            mergedNode.set(field.getKey(), field.getValue());
                        }
                    });
                    return Mono.just(mergedNode);
                })
                .map(this::convertAndSaveLodging)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(this::handleError);
    }

    // 재시도 정책을 설정하는 메서드
    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RetryExhaustedException("재시도 횟수 초과"));
    }

    // API 응답을 Lodging 객체로 변환하고 저장하는 메서드
    private LodgingDetail convertAndSaveLodging(JsonNode jsonNode) {
        try {
            LodgingDetail lodgingDetail = objectMapper.treeToValue(jsonNode, LodgingDetail.class);
            return lodgingDetailRepository.save(lodgingDetail);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json 변환 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }

    

    private Mono<LodgingDetail> handleError(Throwable e) {
        if (e instanceof RetryExhaustedException) {
            return Mono.error(new CustomException("재시도 횟수를 초과하였습니다. 다시 한 번 재시도 해주세요", HttpStatus.SERVICE_UNAVAILABLE));
        } else if (e instanceof UnsupportedMediaTypeException) {
            logger.error("숙소 디테일, 잘못된 주소로 요청", e);
            return Mono.error(new UnsupportedMediaTypeException("잘못된 주소로 요청"));
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            logger.error("api 요청 시간 초과", e);
            return Mono.error(new java.util.concurrent.TimeoutException("요청 시간이 초과되었습니다."));
        } else if( e instanceof NullPointerException){
            logger.error("null값을 조회", e);
            return Mono.error(new NullPointerException("서버 내부 오류가 발생했습니다."));
        }
        else {
            logger.error("알 수 없는 에러 발생", e);
            return Mono.error(new RuntimeException("알 수 없는 에러가 발생했습니다."));
        }
    }

}