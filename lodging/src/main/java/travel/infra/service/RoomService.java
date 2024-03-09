package travel.infra.service;

import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.entity.Room;
import travel.domain.repository.RoomRepository;
import travel.exception.CustomException;
import travel.exception.RetryExhaustedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 숙박 정보를 책임지는 서비스 (전체 숙소 정보 조회, 숙소 상세 정보는 숙박 정보를 책임지기에 단일 원칙 책임에 적합)
public class RoomService {

    private final WebClient webClient;

    private final RoomRepository roomRepository;

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.mobileOS}")
    private String mobileOS;

    @Value("${api.mobileApp}")
    private String mobileApp;

    @Value("${api.arrange}")
    private String arrange;

    // 룸 요청 메서드
    @Transactional
    public Flux<Room> searchRoom(String contentid, String contenttypeid, String type) {
        String urlForRoom = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailInfo1?ServiceKey=%s&contentTypeId=%s&contentId=%s&MobileOS=%s&MobileApp=%s&_type=%s",
                serviceKey, contenttypeid, contentid, mobileOS, mobileApp, type);

        logger.info("룸 요청");
        return webClient.get()
                .uri(URI.create(urlForRoom))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .retryWhen(createRetrySpec())
                .flatMapMany(jsonNode -> Flux 
                                              
                        .fromIterable(
                                jsonNode.path("response").path("body").path("items").path("item")))
                .map(this::convertAndSaveLodging)

                .timeout(Duration.ofSeconds(15))
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
    private Room convertAndSaveLodging(JsonNode jsonNode) {
        try {
            logger.info("룸 요청 완료, DB에 저장 시작");
            Room room = objectMapper.treeToValue(jsonNode, Room.class);
            logger.info("DB 저장 완료");
            return roomRepository.save(room);
        } catch (JsonProcessingException e) {
            logger.error("Json 변환 중 오류 발생", e);
            throw new RuntimeException("Json 변환 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }

    // 오류를 받는 메서드
    private Mono<Room> handleError(Throwable e) {
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