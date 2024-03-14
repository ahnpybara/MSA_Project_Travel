package travel.infra;

import java.net.URI;
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
import travel.domain.Flight;
import travel.domain.FlightRepository;
import travel.exception.JsonTranferException;
import travel.exception.SaveDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Service
public class FlightAPIService {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    private final FlightRepository flightRepository;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    @Value("${api.serviceKey}")
    private String serviceKey;

    // 항공편 API를 호출한 뒤, 이를 DB에 저장하는 메서드입니다.
    public Flux<Flight> callApi(String depAirportId, String arrAirportId, String depPlandTime) {

        // 1부터 4까지의 숫자를 포함하는 Flux를 생성합니다. 이때 Flux의 각 요소는 페이지 번호를 나타냅니다.
        return Flux.range(1, 4)
                .flatMap(page -> {
                    String urlForData = String.format(
                            "http://apis.data.go.kr/1613000/DmstcFlightNvgInfoService/getFlightOpratInfoList?serviceKey=%s&pageNo=%d&numOfRows=4&_type=json&depAirportId=%s&arrAirportId=%s&depPlandTime=%s&airlineId=",
                            serviceKey, page, depAirportId, arrAirportId, depPlandTime);

                    return webClient.get()
                            .uri(URI.create(urlForData))
                            .header("Accept", "application/json")
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .flatMapMany(jsonNode -> {
                                JsonNode itemsNode = jsonNode.path("response").path("body").path("items").path("item");
                                if (itemsNode.isArray()) {
                                    // 배열 형태일 때
                                    return Flux.fromIterable(itemsNode);
                                } else if (!itemsNode.isMissingNode()) {
                                    // 단일 객체일 때
                                    return Flux.just(itemsNode);
                                } else {
                                    // 빈 응답 또는 예상치 못한 형태일 때
                                    return Flux.empty();
                                }
                            })
                            .map(this::convertAndSaveFlight)
                            .onErrorResume(this::handleError);
                }, 4); // 병렬처리를 위해 4개의 쓰레드를 사용하도록 설정하였습니다.
    }

    // API 응답을 Flight 객체로 변환하고 DB에 저장하는 메서드입니다
    // TODO 저장중 문제가 발생했을 경우 롤백을 진행해야합니다
    private Flight convertAndSaveFlight(JsonNode jsonNode) {
        try {
            Flight flight = objectMapper.treeToValue(jsonNode, Flight.class);
            return flightRepository.save(flight);
        } catch (JsonProcessingException e) {
            throw new JsonTranferException("Json 변환 중 오류가 발생했습니다");
        } catch (Exception e) {
            throw new SaveDataException("항공편 저장 중 오류가 발생했습니다");
        }
    }

    // 예외 발생시 실행되는 예외 핸들러 메서드입니다
    private Mono<Flight> handleError(Throwable e) {
        if (e instanceof JsonTranferException) {
            logger.error("\nJson 파싱 도중 예외가 발생했습니다.\n 오류 내용 : " + e + "\n");
            return Mono.error(new JsonTranferException(e.getMessage()));
        } else if (e instanceof SaveDataException) {
            logger.error("\n항공편 정보를 저장하는 도중 오류가 발생했습니다.\n 오류 내용 : " + e + "\n");
            return Mono.error(new SaveDataException(e.getMessage()));
        } else if (e instanceof WebClientResponseException) {
            logger.error("\n항공편 API 서버와 연결을 할 수 없습니다.\n 오류 내용 : " + e + "\n");
            return Mono.error(new RuntimeException("API 서버와 연결할 수 없습니다"));
        } else {
            logger.error("\n항공편 API를 불러오고 저장하는 도중 알 수 없는 오류가 발생했습니다.\n 오류 내용 : " + e + "\n");
            return Mono.error(new Exception("알 수 없는 오류가 발생했습니다"));
        }
    }
}