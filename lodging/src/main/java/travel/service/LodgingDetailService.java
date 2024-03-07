package travel.service;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.Lodging;
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
                .retryWhen(createRetrySpec())
                .flatMap(jsonNode -> Mono.justOrEmpty(// Mono.justOrEmpty: 이 메소드는 주어진 객체를 이용하여 새로운 Mono를 생성하되, 객체가 null인
                                                      // 경우에는 빈 Mono를 반환
                        jsonNode.path("response").path("body").path("items").path("item").get(0)));// 첫번째 아이템을 가져옴
                
                
        // get(0)을 호출하면, 시스템은 예외를 발생시킵니다. 이는 리스트가 비어있음을 즉시 알아챌 수 있게 해주므로, 에러를 빠르게 찾고 수정할
        // 수 있습니다.

        Mono<JsonNode> detailIntro = webClient.get()
                .uri(URI.create(urlForDetailIntro))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .retryWhen(createRetrySpec())

                .flatMap(jsonNode -> Mono.justOrEmpty(
                        jsonNode.path("response").path("body").path("items").path("item").get(0)));// 첫번째 아이템을 가져옴

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
            throw new CustomException("재시도 횟수를 초과하였습니다. 다시 한 번 재시도 해주세요", HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            throw new RuntimeException("예외가 발생하였습니다.");
        }
    }

}