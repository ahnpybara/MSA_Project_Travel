package travel.infra;

import travel.domain.Flight;
import travel.domain.FlightRepository;
import travel.domain.FlightbookCancelled;
import travel.domain.PaymentRequested;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Value("${api.serviceKey}")
    private String serviceKey;

    // 스프링 프레임워크에서 제공하는 HTTP 통신을 담당하는 클래스로, 이를 이용해서 HTTP 요청을 보냅니다.
    private RestTemplate restTemplate = new RestTemplate();

    // 공항의 ID와 공항의 이름을 연결하는 맵을 선언합니다. 이 맵은 서비스가 초기화될 때 채워집니다.
    private Map<String, String> airportIdToNmMap;

    // 이 메서드는 이 클래스의 인스턴스 생성 후에 자동으로 호출되며, 공항의 ID와 이름을 맵에 채워넣습니다.
    @PostConstruct
    public void init() {
        airportIdToNmMap = new HashMap<>();
        airportIdToNmMap.put("NAARKJB", "무안");
        airportIdToNmMap.put("NAARKJJ", "광주");
        airportIdToNmMap.put("NAARKJK", "군산");
        airportIdToNmMap.put("NAARKJY", "여수");
        airportIdToNmMap.put("NAARKNW", "원주");
        airportIdToNmMap.put("NAARKNY", "양양");
        airportIdToNmMap.put("NAARKPC", "제주");
        airportIdToNmMap.put("NAARKPK", "김해");
        airportIdToNmMap.put("NAARKPS", "사천");
        airportIdToNmMap.put("NAARKPU", "울산");
        airportIdToNmMap.put("NAARKSI", "인천");
        airportIdToNmMap.put("NAARKSS", "김포");
        airportIdToNmMap.put("NAARKTH", "포항");
        airportIdToNmMap.put("NAARKTN", "대구");
        airportIdToNmMap.put("NAARKTU", "청주");
    }

    // 공항의 ID를 입력받아 해당하는 공항의 이름을 반환하는 메서드입니다
    private String convertAirportIdToNm(String airportId) {
        return Optional.ofNullable(airportIdToNmMap.get(airportId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid airport ID: " + airportId));
    }

    // 출발 공항 ID, 도착 공항 ID, 출발 시간, 도착 시간을 인자로 받아 해당 조건에 맞는 항공편을 찾아 리스트로 반환하는 메서드입니다
    public List<Flight> findFlights(String depAirportId, String arrAirportId, Long startTimestamp, Long endTimestamp) {

        // 현재 시간 정보를 가져와서 타입에 맞게 변환
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        Long currentTimestamp = Long.parseLong(currentTime.format(formatter));
        // 당일날 예약시 이전 시간대 항공편은 보여주면 안되기 때문에 출발 날짜를 현재시간으로 수정합니다
        if (startTimestamp <= currentTimestamp) startTimestamp = currentTimestamp;

        try {
            String depAirportNm = convertAirportIdToNm(depAirportId);
            String arrAirportNm = convertAirportIdToNm(arrAirportId);
            return flightRepository.findByDepAirportAndArrAirportAndDepTimeBetweenAndSeatCapacityGreaterThanEqual(
                    depAirportNm, arrAirportNm, startTimestamp, endTimestamp, 0L);
        } catch (Exception e) {
            System.out.println("공항Id의 정보가 잘못되었습니다 : " + e);
            throw new IllegalArgumentException(e);
        }
    }

    // 출발 공항 ID, 도착 공항 ID, 출발 예정 시각을 인자로 받아 항공편 API를 호출하여 항공편 정보를 가져오는 메서드입니다
    public String callApi(String depAirportId, String arrAirportId, String depPlandTime) {
        String url = "http://apis.data.go.kr/1613000/DmstcFlightNvgInfoService/getFlightOpratInfoList"
                + "?serviceKey=" + serviceKey
                + "&pageNo=1"
                + "&numOfRows=10"
                + "&_type=json"
                + "&depAirportId=" + depAirportId
                + "&arrAirportId=" + arrAirportId
                + "&depPlandTime=" + depPlandTime
                + "&airlineId=";

        try {
            URI uri = new URI(url); // 주어진 URL 문자열을 URI 객체로 변환합니다. + 인코딩

            // RestTemplate를 사용하여 API를 호출하고, 그 결과를 문자열 형태로 반환합니다.
            return restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            System.out.println("항공편 api를 호출하는 도중 예상치 못한 오류가 발생했습니다 : " + e);
            throw new IllegalArgumentException("Failed Flight API Call : " + e);
        }
    }

    // 항공편 API에서 받아온 JSON 형태의 문자열을 Flight 객체로 변환하고, 그 결과를 데이터베이스에 저장합니다
    @Transactional(rollbackFor = RollBackException.class)
    public void saveFlightData(String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱에 사용되는 클래스의 인스턴스를 생성합니다

        try {
            // JSON 문자열을 JsonNode 객체로 변환함으로써, JSON 데이터를 쉽게 다룰 수 있습니다.
            JsonNode rootNode = objectMapper.readTree(jsonData);
            // JsonNode 객체에서 "response" -> "body" -> "items" -> "item" 순서로 데이터를 추출합니다.
            JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

            if (!itemsNode.isMissingNode() && !itemsNode.isEmpty()) {
                // 배열 형태의 데이터를 Flight 객체의 리스트로 변환합니다.
                List<Flight> flights = objectMapper.readValue(itemsNode.toString(), new TypeReference<List<Flight>>() {
                });

                // Flight 객체의 리스트를 순회하며 각각의 Flight 객체를 데이터베이스에 저장합니다.
                for (Flight flight : flights)
                    flightRepository.save(flight);

            } else {
                System.out.println("항공편 정보가 존재하지 않습니다");
            }
        } catch (Exception e) {
            System.out.println("Failed to save Flights : " + e);
            throw new RollBackException("롤백 트랜잭션"); // DB와 상화작용하는 메서드이므로 예외 발생시 트랜잭션을 롤백시키도록 합니다
        }
    }

    // 예약 요청이 되었을 때 해당 항공편의 좌석수를 감소
    @Transactional(rollbackFor = Exception.class)
    public void bookSeatCapacity(PaymentRequested paymentRequested) {
        try {
            Flight flight = flightRepository.findById(paymentRequested.getFlightId())
                    .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
            if (flight.getSeatCapacity() <= 0) throw new IllegalArgumentException("No more seats available");
            flight.setSeatCapacity(flight.getSeatCapacity() - 1);
            flightRepository.save(flight);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 예약 취소가 완료 되었을 때 해당 항공편의 좌석수를 증가
    @Transactional(rollbackFor = Exception.class)
    public void cancelSeatCapacity(FlightbookCancelled flightbookCancelled) {
        try {
            Flight flight = flightRepository.findById(flightbookCancelled.getFlightId())
                    .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
            flight.setSeatCapacity(flight.getSeatCapacity() + 1);
            flightRepository.save(flight);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}