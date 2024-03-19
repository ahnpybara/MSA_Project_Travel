package travel.service;

import java.net.URI;
import java.net.UnknownHostException;
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

import io.netty.channel.ConnectTimeoutException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.LodgingIntro;
import travel.exception.CustomException;
import travel.exception.RetryExhaustedException;
import travel.repository.LodgingIntroRepository;

@Service
@RequiredArgsConstructor
// 숙박 정보를 책임지는 서비스 (전체 숙소 정보 조회, 숙소 상세 정보는 숙박 정보를 책임지기에 단일 원칙 책임에 적합)
public class LodgingIntroService {

    private final WebClient webClient;

    private final LodgingIntroRepository lodgingIntroRepository;

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

    // 숙소 소개 메서드
    public Mono<LodgingIntro> searchIntro(String contentid, String contenttypeid, String type) {
        
        String urlForDetailIntro = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailIntro1?ServiceKey=%s&contentTypeId=%s&contentId=%s&MobileOS=%s&MobileApp=%s&_type=%s",
                serviceKey, contenttypeid, contentid, mobileOS, mobileApp, type);

        Retry retry = createRetrySpec();

        logger.info("숙소 소개 요청");

        return webClient.get()
                .uri(URI.create(urlForDetailIntro))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .retryWhen(retry)

                .flatMap(jsonNode -> {
                    JsonNode itemNode = jsonNode.path("response").path("body").path("items").path("item");
                    if (itemNode.isArray() && itemNode.size() > 0) {
                        return Mono.justOrEmpty(itemNode.get(0));
                    } else {
                        logger.info("intro 빈 객체 반환");
                        return Mono.empty();
                    }
                })
                .map(this::convertAndSaveLodging)
                .timeout(Duration.ofSeconds(6))
                .onErrorResume(this::handleError);
    }

    // 재시도 정책을 설정하는 메서드
    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
                .filter(this::isNetworkException) // 네트워크 오류에 대해서만 재시도합니다.
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RetryExhaustedException("재시도 횟수 초과"));
    }

    // 일어날 수 있는 네트워크 예외
    private boolean isNetworkException(Throwable throwable) {
        return throwable instanceof WebClientResponseException ||
                throwable instanceof ConnectTimeoutException ||
                throwable instanceof UnknownHostException;
    }

    // API 응답을 Lodging 객체로 변환하고 저장하는 메서드
    private LodgingIntro convertAndSaveLodging(JsonNode jsonNode) {
        try {
            logger.info("숙소 소개 요청 완료, DB에 저장 시작");

            LodgingIntro lodgingIntro = objectMapper.treeToValue(jsonNode, LodgingIntro.class);
            logger.info("DB 저장 완료");

            return lodgingIntroRepository.save(lodgingIntro);
        } catch (JsonProcessingException e) {
            logger.error("Json 변환 중 오류 발생", e);
            throw new RuntimeException("Json 변환 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }

    // 오류를 받는 메서드
    private Mono<LodgingIntro> handleError(Throwable e) {
        if (e instanceof RetryExhaustedException) {
            return Mono.error(new CustomException("재시도 횟수를 초과하였습니다. 다시 한 번 재시도 해주세요", HttpStatus.SERVICE_UNAVAILABLE));
        } else if (e instanceof UnsupportedMediaTypeException) {
            logger.error("숙소 디테일, 잘못된 주소로 요청", e);
            return Mono.error(new UnsupportedMediaTypeException("잘못된 주소로 요청"));
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            logger.error("api 요청 시간 초과", e);
            return Mono.error(new java.util.concurrent.TimeoutException("요청 시간이 초과되었습니다."));
        } else if (e instanceof NullPointerException) {
            logger.error("null값을 조회", e);
            return Mono.error(new NullPointerException("서버 내부 오류가 발생했습니다."));
        } else {
            logger.error("알 수 없는 에러 발생", e);
            return Mono.error(new RuntimeException("알 수 없는 에러가 발생했습니다."));
        }
    }

}