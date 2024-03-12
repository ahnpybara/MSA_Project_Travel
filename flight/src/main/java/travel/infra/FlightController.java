package travel.infra;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import travel.domain.Flight;

@RestController
public class FlightController {

    private final FlightAPIService flightAPIService;
    private final FlightService flightService;

    public FlightController(FlightAPIService flightAPIService, FlightService flightService) {
        this.flightAPIService = flightAPIService;
        this.flightService = flightService;
    }

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 항공편 조회 요청을 받아서 항공편 정보를 응답으로 주는 메서드입니다
    @GetMapping("/flights/search")
    public Flux<Flight> getFlightDetails(@RequestParam String depAirport, @RequestParam String arrAirport, @RequestParam String depTime) {

        Long startTimestamp = Long.parseLong(depTime + "0000");
        Long endTimestamp = Long.parseLong(depTime + "2359");

        List<Flight> flights = flightService.findFlights(depAirport, arrAirport, startTimestamp, endTimestamp);

        if (!flights.isEmpty()) {
            logger.info("\nFound flights in the database.\n");
            return Flux.fromIterable(flights);
        } else {
            logger.info("\nNo flights found in the database. Calling the API.\n");
            return flightAPIService.callApi(depAirport, arrAirport, depTime);
        }
    }

    // 항공예약으로부터 좌석수 조회 API 호출을 받기 위한 메서드입니다
    @GetMapping("/flights/seatCapacity")
    public ResponseEntity<Flight> getFlightSeatCapacity(@RequestParam("flightId") Long flightId) {
        logger.info("\n항공 예약서비스로 부터 API 호출이 되었습니다\n");
        Flight flight = flightService.findFlightAPI(flightId);
        return ResponseEntity.ok(flight);
    }
}