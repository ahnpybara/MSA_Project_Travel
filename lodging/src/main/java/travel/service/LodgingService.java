package travel.service;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import travel.domain.Lodging;
import travel.exception.JsonTranferException;
import travel.exception.SaveDataException;
import travel.repository.LodgingRepository;

@Service
@RequiredArgsConstructor
public class LodgingService {

    private final WebClient webClient;

    private final LodgingRepository lodgingRepository;

    private final ObjectMapper objectMapper;

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.mobileOS}")
    private String mobileOS;

    @Value("${api.mobileApp}")
    private String mobileApp;

    @Value("${api.arrange}")
    private String arrange;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 숙소 API로 부터 숙소 정보를 요청해서 받아오는 메서드
    public Flux<Lodging> search(String areaCode, String sigunguCode, int pageNo, int numOfRows, String type) {

        // 해당 지역과 시군구에 존재하는 숙소의 수를 요청하는 URL을 구성합니다
        String urlForTotalCount = String.format(
                "https://apis.data.go.kr/B551011/KorService1/searchStay1?numOfRows=8&pageNo=1&MobileOS=%s&MobileApp=%s&_type=%s&listYN=N&arrange=%s&areaCode=%s&sigunguCode=%s&ServiceKey=%s",
                mobileOS, mobileApp, type, arrange, areaCode, sigunguCode, serviceKey);

        Mono<JsonNode> totalCountResponse = webClient.get()
                .uri(URI.create(urlForTotalCount))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5));

        // 페이지의 수를 구한 뒤, 각 페이지에 대한 요청을 보내는 URL을 구성하고 요청을 보냅니다
        return totalCountResponse.flatMapMany(totalCount -> {
            int totalCnt = totalCount.get("response").get("body").get("items").get("item").get(0).get("totalCnt").asInt();
            int totalPages = (totalCnt + numOfRows - 1) / numOfRows;

            return Flux.range(1, totalPages)
                    .flatMap(page -> {
                        String urlForData = String.format(
                                "http://apis.data.go.kr/B551011/KorService1/searchStay1?areaCode=%s&sigunguCode=%s&ServiceKey=%s&listYN=Y&MobileOS=%s&MobileApp=%s&arrange=%s&numOfRows=%d&pageNo=%d&_type=%s",
                                areaCode, sigunguCode, serviceKey, mobileOS, mobileApp, arrange, numOfRows, page, type);

                        return webClient.get()
                                .uri(URI.create(urlForData))
                                .header("Accept", "application/json")
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .timeout(Duration.ofSeconds(5))
                                .flatMapMany(jsonNode -> Flux
                                        .fromIterable(
                                                jsonNode.path("response").path("body").path("items").path("item")))
                                .map(this::convertToLodgings)
                                .collectList()
                                .flatMapMany(lodgings -> this.saveAllLodgings(lodgings))
                                .onErrorResume(this::handleError);
                    }, totalPages);
        });
    }

    // API 응답을 Lodging 객체로 변환하고 저장하는 메서드
    private Lodging convertToLodgings(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, Lodging.class);
        } catch (JsonProcessingException e) {
            throw new JsonTranferException("Json 변환 중 오류가 발생했습니다");
        }
    }

    // Lodging 객체 List를 한 번에 저장하는 메서드
    private Flux<Lodging> saveAllLodgings(List<Lodging> lodgings) {
        try {
            return Flux.fromIterable(lodgingRepository.saveAll(lodgings));
        } catch (Exception e) {
            throw new SaveDataException("숙소편 저장 중 오류가 발생했습니다");
        }
    }

    // 에러 처리 로직을 최적화한 handleError 메서드
    private Mono<Lodging> handleError(Throwable e) {
        if (e instanceof JsonTranferException) {
            return logAndReturnError("Json 파싱 도중 예외가 발생했습니다.", e, new JsonTranferException(e.getMessage()));
        } else if (e instanceof SaveDataException) {
            return logAndReturnError("숙소 정보를 저장하는 도중 오류가 발생했습니다.", e, new SaveDataException(e.getMessage()));
        } else if (e instanceof WebClientResponseException) {
            return logAndReturnError("API 서버와 연결할 수 없습니다.", e, new RuntimeException("API 서버와 연결할 수 없습니다"));
        } else if (e instanceof TimeoutException) {
            return logAndReturnError("타임아웃이 발생했습니다.", e, new RuntimeException("API 서버와 연결할 수 없습니다"));
        }else {
            return logAndReturnError("알 수 없는 오류가 발생했습니다.", e, new Exception("알 수 없는 오류가 발생했습니다"));
        }
    }

    // 로그를 기록하고 에러를 반환하는 메서드
    private <T> Mono<T> logAndReturnError(String logMessage, Throwable e, Exception returnException) {
        logger.error("\n" + logMessage + "\n 오류 내용 : " + e);
        return Mono.error(returnException);
    }
}