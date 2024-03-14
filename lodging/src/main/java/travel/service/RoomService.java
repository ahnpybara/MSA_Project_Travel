package travel.service;

import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.LongStream;

import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ConnectTimeoutException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import travel.domain.Room;
import travel.events.subscribe.LodgingReservationCancelRequested;
import travel.events.subscribe.LodgingReservationRequested;
import travel.exception.CustomException;
import travel.exception.RetryExhaustedException;
import travel.repository.RoomRepository;

@Service
@RequiredArgsConstructor
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

    // 룸 요청 메서드입니다.
    public Flux<Room> searchRoom(String contentid, String contenttypeid, String type) {
        String urlForRoom = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailInfo1?ServiceKey=%s&contentTypeId=%s&contentId=%s&MobileOS=%s&MobileApp=%s&_type=%s",
                serviceKey, contenttypeid, contentid, mobileOS, mobileApp, type);

        Retry retry = createRetrySpec();

        return webClient.get()
                .uri(URI.create(urlForRoom))
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .retryWhen(retry)
                .flatMapMany(jsonNode -> Flux

                        .fromIterable(
                                jsonNode.path("response").path("body").path("items").path("item")))
                .map(this::convertAndSaveLodging)

                .timeout(Duration.ofSeconds(10))
                .onErrorResume(this::handleError);

    }

    // 재시도 정책을 설정하는 메서드
    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
                .filter(this::isNetworkException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RetryExhaustedException("재시도 횟수 초과"));
    }

    // 일어날 수 있는 네트워크 예외
    private boolean isNetworkException(Throwable throwable) {
        return throwable instanceof WebClientResponseException ||
                throwable instanceof ConnectTimeoutException ||
                throwable instanceof UnknownHostException;
    }

    public Long getRoomCapacityByDate(Long roomCode, Long reservationDate) {
        try {
            Room room = roomRepository.findByRoomcode(roomCode)
                    .orElseThrow(() -> new IllegalArgumentException("해당 roomCode를 찾을수 없습니다. " + roomCode));

            logger.info("\n 해당 숙소를 찾았습니다.");
            String dateString = String.valueOf(reservationDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localReservationDate = LocalDate.parse(dateString, formatter);
            Map<LocalDate, Long> roomCapacity = room.getRoomCapacity();

            return roomCapacity.getOrDefault(localReservationDate, -1L);
        } catch (IllegalArgumentException e) {
            logger.error("예약된 객실을 찾을 수 없습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new RuntimeException("알수 없는 오류 발생", e);
        }
    }

    // 객실 수를 감소하는 메서드
    @Transactional(rollbackFor = { RollbackException.class })
    public void decreaseRoomCapacity(LodgingReservationRequested lodgingReservationRequested) {
        try {
            String dateString = String.valueOf(lodgingReservationRequested.getReservationDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate reservationDate = LocalDate.parse(dateString, formatter);

            Room room = roomRepository.findByRoomcode(
                    lodgingReservationRequested.getRoomCode())
                    .orElseThrow(() -> new IllegalArgumentException("예약 요청된 방의 정보를 확인할 수 없습니다."));

            Map<LocalDate, Long> roomCapacity = room.getRoomCapacity();

            roomCapacity.compute(reservationDate, (key, value) -> {
                if (value == null || value <= 0) {
                    throw new IllegalStateException("예약 가능한 객실이 없습니다.");
                }
                return value - 1;
            });

            room.setRoomCapacity(roomCapacity);
            roomRepository.save(room);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("예약된 객실을 찾을 수 없거나, 남은 객실의 수가 부족합니다.");
            throw e;
        } catch (Exception e) {
            logger.error("알 수 없는 오류로 해당 객실의 수를 감소하는데 실패했습니다.");
            throw new RollbackException("알 수 없는 오류가 발생했습니다.");
        }
    }

    // 객실 수를 증가하는 메서드
    @Transactional(rollbackFor = RollbackException.class)
    public void IncreaseRoomCapacity(LodgingReservationCancelRequested lodgingReservationCancelRequested) {
        try {

            String dateString = String.valueOf(lodgingReservationCancelRequested.getReservationDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate reservationDate = LocalDate.parse(dateString, formatter);

            Room room = roomRepository.findByRoomcode(
                    lodgingReservationCancelRequested.getRoomCode())
                    .orElseThrow(() -> new IllegalArgumentException("예약 취소 요청된 객실의 정보를 확인할 수 없습니다."));

            Map<LocalDate, Long> roomCapacity = room.getRoomCapacity();
            roomCapacity.compute(reservationDate, (key, value) -> {
                if (value >= 1) {
                    throw new IllegalStateException("객실의 수가 MAX입니다.");
                }
                return value + 1;
            });
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("예약된 객실을 찾을 수 없거나, 객실의 수가 MAX입니다.");
            throw e;
        } catch (Exception e) {
            logger.error("알 수 없는 오류로 해당 객실의 수를 증가하는데 실패했습니다.");
            throw new RollbackException("알 수 없는 오류가 발생했습니다.");
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

    // API 응답을 Room 객체로 변환하고 저장하는 메서드
    private Room convertAndSaveLodging(JsonNode jsonNode) {
        try {
            logger.info("룸 요청 완료, DB에 저장 시작");
            Room room = objectMapper.treeToValue(jsonNode, Room.class);
            Map<LocalDate, Long> roomCapacity = new ConcurrentHashMap<>();

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(29); 

            LongStream.rangeClosed(0, ChronoUnit.DAYS.between(startDate, endDate)).parallel().forEach(day -> {
                LocalDate date = startDate.plusDays(day);
                roomCapacity.put(date, 1L); 
            });

            room.setRoomCapacity(roomCapacity);

            logger.info("DB 저장 완료");
            return roomRepository.save(room);
        } catch (JsonProcessingException e) {
            logger.error("Json 변환 중 오류 발생", e);
            throw new RuntimeException("Json 변환 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }
}