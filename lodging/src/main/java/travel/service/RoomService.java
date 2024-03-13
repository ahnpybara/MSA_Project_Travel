package travel.service;

import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

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
import travel.events.subscribe.LodgingReservationCancelled;
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

    //객실 수를 감소하는 메서드
    @Transactional(rollbackFor = { RollbackException.class })
    public void decreaseRoomCapacity(LodgingReservationRequested lodgingReservationRequested) {
        try {
            //Long 타입으로 받은 예약 날짜를 String으로 변환, DateTimeFormatter를 사용하려면 Long을 사용할 수 없기 때문
            String dateString = String.valueOf(lodgingReservationRequested.getReservationDate());
            
            //날짜 패턴을 yyyyMMdd로 설정, 각각 일치하는 자리에 맞게 날짜로 인식합니다. 
            //20240314라는 문자열을 yyyyMMdd 패턴 적용 예시
            //yyyy MM dd
            //2024 03 14 로 패턴 적용
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            
            //문자열에 패턴을 적용시켜 LocalDate 타입으로 변환
            LocalDate reservationDate = LocalDate.parse(dateString, formatter);

            //객실의 코드로 예약하고자 하는 객실을 조회
            Room room = roomRepository.findByRoomcode(
                    lodgingReservationRequested.getRoomCode())
                    .orElseThrow(() -> new IllegalArgumentException("예약 요청된 방의 정보를 확인할 수 없습니다."));

            //roomCapacity 데이터를 불러옴
            Map<LocalDate, Long> roomCapacity = room.getRoomCapacity(); 

            //compute 는 키의 값을 재설정하는 메서드
            //(key, value)는 람다 형식으로 compute 메서드에 정의된 일반적인 표현 방식
            //key는 reservationDate를 나타내며 value는 값을 나타냄
            //reservationDate와 일치하는 키의 값을 재설정
            //값이 null이거나 0보다 작으면 예외 발생
            //존재한다면 값을 value -1 하고 저장
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

    //객실 수를 증가하는 메서드
    @Transactional(rollbackFor = RollbackException.class)
    public void IncreaseRoomCapacity(LodgingReservationCancelled lodgingReservationCancelled) {
        try {
            
            String dateString = String.valueOf(lodgingReservationCancelled.getReservationDate());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate reservationDate = LocalDate.parse(dateString, formatter);

            Room room = roomRepository.findByRoomcode(
                    lodgingReservationCancelled.getRoomCode())
                    .orElseThrow(() -> new IllegalArgumentException("예약 취소 요청된 객실의 정보를 확인할 수 없습니다."));

            Map<LocalDate, Long> roomCapacity = room.getRoomCapacity(); 
            roomCapacity.compute(reservationDate, (key, value) -> {
                if (value >= 10) {
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

            //Map 타입으로 객체 생성, !!ConcurrentHashMap 이건 아직 몰라서 공부하고 있는데 멀티 스레드 환경 좋은 동시성 테이블이라고 합니다.!!
            Map<LocalDate, Long> roomCapacity = new ConcurrentHashMap<>();

            //년과 월을 나타내는 클래스, 인자를 (2024, 3)으로 지정함으로 yearMonth은 2024-03이 됨
            YearMonth yearMonth = YearMonth.of(2024, 3);
            //lengthOfMonth을 사용해 2024년 3월의 일수를 계산
            int daysInMonth = yearMonth.lengthOfMonth();

            //병렬처리 방식
            //1에서 daysInMonth를 포함한 정수 스트림을 생성 ex) (1, 4) 라면 1, 2, 3, 4 로 4를 포함
            //parallel() 병렬처리 메서드
            IntStream.rangeClosed(1, daysInMonth).parallel().forEach(day -> {
                //atDay, yearMonth에 day를 결합
                LocalDate date = yearMonth.atDay(day);
                //roomCapacity는 Map 객체이기 때문에 put으로 키와 값을 설정, date라는 날짜에 10개의 객실 수를 지정, L은 long이라는 타입
                roomCapacity.put(date, 10L);
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