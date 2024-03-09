package travel.infra.service;

import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.entity.Lodging;
import travel.domain.repository.LodgingRepository;
import travel.exception.CustomException;
import travel.exception.RetryExhaustedException;

@Service
@RequiredArgsConstructor
//숙박 정보를 책임지는 서비스 (전체 숙소 정보 조회, 숙소 상세 정보는 숙박 정보를 책임지기에 단일 원칙 책임에 적합)
public class LodgingService {

    private final WebClient webClient;

    private final LodgingRepository lodgingRepository;

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(LodgingService.class);

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.mobileOS}")
    private String mobileOS;

    @Value("${api.mobileApp}")
    private String mobileApp;

    @Value("${api.arrange}")
    private String arrange;
    
    public Flux<Lodging> search(String areaCode, String sigunguCode, int pageNo, int numOfRows, String type) {

        // 재시도 정책을 설정합니다. 최대 3번 재시도하며, 재시도 간격은 처음에는 1초로 시작해서 최대 10초까지 증가합니다.
        Retry retrySpec = createRetrySpec();

        // 첫 번째 요청 URL : 전체 데이터 개수를 받아오는 API 요청 URL을 생성합니다.
        String urlForTotalCount = String.format(
                "http://apis.data.go.kr/B551011/KorService1/searchStay1?MobileOS=%s&MobileApp=%s&_type=%s&listYN=%s&arrange=%s&areaCode=%s&ServiceKey=%s",
                mobileOS, mobileApp, type, "N", arrange, areaCode, serviceKey);

                logger.info("첫 번쨰 요청 시작");
        // WebClient를 사용하여 전체 데이터 개수를 받아오는 요청을 보냅니다.
        Mono<JsonNode> totalCountResponse = webClient.get()
                .uri(URI.create(urlForTotalCount))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class);

        logger.info("첫 번쨰 요청 완료");
        // 전체 데이터의 수를 이용하여, 각 페이지에 대한 API 요청을 보내는 Flux를 생성하여 반환합니다.
        return totalCountResponse.flatMapMany(totalCount -> {
            int totalCnt = totalCount.get("response").get("body").get("items").get("item").get(0).get("totalCnt").asInt(); // 전체 데이터 개수를 JsonNode에서 추출합니다
            int totalPages = (totalCnt + numOfRows - 1) / numOfRows; // 전체 페이지 수를 계산합니다.
            // 1부터 totalPages까지의 숫자를 포함하는 Flux를 생성합니다. 이때 Flux의 각 요소는 페이지 번호를 나타냅니다.
            logger.info("두 번쨰 요청 시작");
            return Flux.range(1, totalPages)
                    .flatMap(page -> {

                        // 두 번째 요청 URL : 각 페이지에 대한 데이터를 받아오는 API 요청 URL을 생성합니다
                        String urlForData = String.format(
                                "http://apis.data.go.kr/B551011/KorService1/searchStay1?areaCode=%s&sigunguCode=%s&ServiceKey=%s&listYN=Y&MobileOS=%s&MobileApp=%s&arrange=%s&numOfRows=%d&pageNo=%d&_type=%s",
                                areaCode, sigunguCode, serviceKey, mobileOS, mobileApp, arrange, numOfRows, page, type);

                        // 각 페이지들에에 대한 URL을 생성하고, 이 URL에 대해 HTTP GET 요청을 보내는 WebClient를 생성하여 요청을 보낸 뒤,
                        // API 응답 본문을 Mono<JsonNode>로 변환합니다. 그리고 이 Mono<JsonNode>들은 이후의 flatMap 연산자에 의해
                        // 처리됩니다
                        return webClient.get()
                                .uri(URI.create(urlForData))
                                
                                .header("Accept", "application/json")
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .flatMapMany(jsonNode -> Flux // 각 페이지의 API 응답의 본문을 처리하는 부분으로, flatMapMany 연산자를 사용하여 각
                                                              // 응답 본문의 데이터들을 처리한 뒤, 이를 하나의 Flux로 변환합니다.
                                        .fromIterable(
                                                jsonNode.path("response").path("body").path("items").path("item")))
                                .map(this::convertAndSaveLodging) // map 함수는 API 응답의 각 아이템(위에서 처리된(추출된)Flux의 각 아이템)을
                                                                  // Lodging 객체로 변환하고, 이를 데이터베이스에 저장하는 작업을 수행합니다.
                                .timeout(Duration.ofSeconds(10)) // 여기에 타임아웃을 추가
                                
                                .retryWhen(retrySpec) // 오류 발생 시 재시도를 실행하는 핸들러를 적용
                                .onErrorResume(this::handleError); // 모든 재시도가 실패한 후, 또는 재시도를 하지 않는 경우에 오류를 처리하는 핸들러
                                
                    }, totalPages);
                    
        });
        
    }

    // 재시도 정책을 설정하는 메서드
    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RetryExhaustedException("재시도 횟수 초과"));
    }

    // API 응답을 Lodging 객체로 변환하고 저장하는 메서드
    private Lodging convertAndSaveLodging(JsonNode jsonNode) {
        try {
            Lodging lodging = objectMapper.treeToValue(jsonNode, Lodging.class);
            return lodgingRepository.save(lodging);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json 변환 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }

    // search 메서드 내에서 예외 발생시 실행되는 예외 핸들러 메서드
    private Mono<Lodging> handleError(Throwable e) {
        if (e instanceof RetryExhaustedException) {
            throw new CustomException("재시도 횟수를 초과하였습니다. 다시 한 번 재시도 해주세요", HttpStatus.SERVICE_UNAVAILABLE);
        } else if(e instanceof UnsupportedMediaTypeException){
            logger.error("전체 숙소 조희, 잘못된 주소 요청", e);;
            return Mono.error(new UnsupportedMediaTypeException("잘못된 주소로 요청"));
        } else if (e instanceof WebClientResponseException) {
            throw new CustomException("서버와 연결할 수 없습니다 : " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (e instanceof RuntimeException) {
            throw new CustomException("숙소 정보를 저장하는 도중 오류가 발생했습니다 : " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            throw new CustomException("알 수 없는 오류 : " + e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
        }
    }

   
}