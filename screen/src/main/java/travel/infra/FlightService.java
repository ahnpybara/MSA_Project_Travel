package travel.infra;

import travel.domain.Flight;
import travel.domain.FlightRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

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
        // 맵에서 공항의 ID를 키로 사용하여 공항의 이름을 찾습니다. 해당 ID가 맵에 없을 경우 예외를 발생시킵니다.
        return Optional.ofNullable(airportIdToNmMap.get(airportId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid airport ID: " + airportId));
    }

    // 출발 공항 ID, 도착 공항 ID, 출발 시간, 도착 시간을 인자로 받아 해당 조건에 맞는 항공편을 찾아 리스트로 반환하는 메서드입니다
    public List<Flight> findFlights(String depAirportId, String arrAirportId, Long startTimestamp, Long endTimestamp) {
        // 출발 공항 ID와 도착 공항 ID를 각각의 공항 이름으로 변환합니다.
        String depAirportNm = convertAirportIdToNm(depAirportId);
        String arrAirportNm = convertAirportIdToNm(arrAirportId);
        // 출발 공항 이름, 도착 공항 이름, 출발 시간, 도착 시간을 조건으로 하는 항공편을 찾습니다. 만약 해당 조건에 맞는 항공편이 없다면 빈 리스트를 반환합니다
        return Optional.ofNullable(flightRepository.findByDepAirportAndArrAirportAndDepTimeBetween(depAirportNm, arrAirportNm, startTimestamp, endTimestamp))
                .orElse(Collections.emptyList());
    }

    // 출발 공항 ID, 도착 공항 ID, 출발 예정 시각을 인자로 받아 항공편 API를 호출하여 항공편 정보를 가져오는 메서드입니다
    public String callApi(String depAirportId, String arrAirportId, String depPlandTime) throws URISyntaxException {
        String url = "http://apis.data.go.kr/1613000/DmstcFlightNvgInfoService/getFlightOpratInfoList"
                + "?serviceKey=O%2B0DdibyeRPwjChz%2BqSJN%2FEurIanim0THVar8SxizDrSwO9bDs%2BJWH5YxshTVo5qudULTKjhTOOUyxZSrjD9oQ%3D%3D"
                + "&pageNo=1"
                + "&numOfRows=10"
                + "&_type=json"
                + "&depAirportId=" + depAirportId
                + "&arrAirportId=" + arrAirportId
                + "&depPlandTime=" + depPlandTime
                + "&airlineId=";

        URI uri = new URI(url); // 주어진 URL 문자열을 URI 객체로 변환합니다. + 인코딩

        // RestTemplate를 사용하여 API를 호출하고, 그 결과를 문자열 형태로 반환합니다.
        return restTemplate.getForObject(uri, String.class);
    }

    // 항공편 API에서 받아온 JSON 형태의 문자열을 Flight 객체로 변환하고, 그 결과를 데이터베이스에 저장한 뒤 저장된 Flight 객체들을 반환하는 메서드입니다
    public List<Flight> saveFlightData(String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱에 사용되는 클래스의 인스턴스를 생성
        List<Flight> savedFlights = new ArrayList<>();  // 데이터베이스에 저장된 Flight 객체들을 담을 리스트를 생성합니다

        try {
            // JSON 문자열을 JsonNode 객체로 변환함으로써, JSON 데이터를 쉽게 다룰 수 있습니다.
            JsonNode rootNode = objectMapper.readTree(jsonData);

            // JsonNode 객체에서 "response" -> "body" -> "items" -> "item" 순서로 데이터를 추출합니다.
            JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

            // 추출된 데이터를 확인합니다.
            if (itemsNode != null && itemsNode.isArray()) {
                // 배열 형태의 데이터를 Flight 객체의 리스트로 변환합니다.
                List<Flight> flights = objectMapper.readValue(itemsNode.toString(), new TypeReference<List<Flight>>() {});

                // Flight 객체의 리스트를 순회하며 각각의 Flight 객체를 데이터베이스에 저장합니다.
                for (Flight flight : flights) {
                    Flight savedFlight = flightRepository.save(flight);
                    savedFlights.add(savedFlight);   // 저장된 Flight 객체를 리스트에 추가합니다.
                }
            } else {
                // "item"이 배열이 아니라면 데이터 형식이 잘못된 것이므로 예외를 발생시킵니다.
                throw new IllegalArgumentException("Invalid data format");
            }
        } catch (IOException e) {
            // 데이터베이스에 데이터를 저장하는 과정에서 오류가 발생하면 RuntimeException을 발생시킵니다.
            throw new RuntimeException("Failed to save flight data", e);
        }
        return savedFlights; // 데이터베이스에 저장된 Flight 객체들을 반환합니다.
    }
}
