package travel.infra;

import java.net.URISyntaxException;
import java.util.List;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import travel.domain.*;

@RestController
@Transactional
public class FlightController {

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    private FlightService flightService;

    @GetMapping("/flights/search")
    public List<Flight> getAndSaveFlightData(@RequestParam String depAirportId, @RequestParam String arrAirportId,
            @RequestParam String depPlandTime) {

        // DB에는 년/월/일/시/분 단위로 저장되지만, 전달되는 데이터는 년/월/일 단위로 전달되기 때문에 DB 조회를 위해서 형식을 맞춤
        Long startTimestamp = Long.parseLong(depPlandTime + "0000");
        Long endTimestamp = Long.parseLong(depPlandTime + "2359");

        // 출발 공항 ID, 도착 공항 ID, 출발 시간, 도착 시간을 조건으로 하는 항공편을 찾습니다..
        List<Flight> flights = flightService.findFlights(depAirportId, arrAirportId, startTimestamp, endTimestamp);

        // 파라미터 유효성 검사 필요?? 감이 안옴..

        // 해당 조건에 맞는 항공편이 없다면 API를 호출하여 항공편 정보를 가져와 데이터베이스에 저장하고, 저장된 항공편 정보를 반환합니다.
        if (flights.isEmpty()) {
            System.out.println("++++++++++++++++++++++++++++저장로직 실행!!");
            try {
                String flightData = flightService.callApi(depAirportId, arrAirportId, depPlandTime);
                return flightService.saveFlightData(flightData);
            } catch (URISyntaxException e) {
                // API를 호출하는 과정에서 오류가 발생하면 HTTP 상태 코드 400과 함께 오류 메시지를 반환합니다.
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "An error occurred while retrieving data. Please check your request and try again.", e);
            }
        } else {
            // 해당 조건에 맞는 항공편이 있다면 그 항공편 정보를 반환합니다.
            System.out.println("++++++++++++++++++++++++++++조회로직 실행!!");
            return flights;
        }
    }
}