package travel.infra;

import java.util.List;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import travel.domain.*;

@RestController
@Transactional
public class FlightController {

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    private FlightService flightService;

    @PostMapping("/flights/search")
    public ResponseEntity<?> getAndSaveFlightData(@RequestBody FlightDTO request) {

        String depAirportId = request.getDepAirport();
        String arrAirportId = request.getArrAirport();
        String depPlandTime = request.getDepTime();

        // DB에는 년/월/일/시/분 단위로 저장되지만, 전달되는 데이터는 년/월/일 단위로 전달되기 때문에 DB 조회를 위해서 형식을 맞춤
        Long startTimestamp = Long.parseLong(depPlandTime + "0000");
        Long endTimestamp = Long.parseLong(depPlandTime + "2359");

        try {
            // 출발 공항 ID, 도착 공항 ID, 출발 시간, 도착 시간을 조건으로 하는 항공편을 찾습니다.
            // 해당 조건에 맞는 항공편이 없다면 API를 호출하여 항공편 정보를 가져와 데이터베이스에 저장하고, 저장된 항공편 정보를 반환합니다.
            List<Flight> flights = flightService.findFlights(depAirportId, arrAirportId, startTimestamp, endTimestamp);

            if (flights.isEmpty()) {
                System.out.println("++++++++++++++++++++++++++++저장로직 실행!!");
                String flightData = flightService.callApi(depAirportId, arrAirportId, depPlandTime);

                if (flightData == null || flightData.isEmpty()) throw new Exception("항공편 데이터를 가져오는 데 실패했습니다.");
                
                List<Flight> savedFlights = flightService.saveFlightData(flightData);

                if (savedFlights == null) throw new Exception("항공편 정보 저장에 실패했습니다.");

                return ResponseEntity.ok(savedFlights);
            } else {
                System.out.println("++++++++++++++++++++++++++++조회로직 실행!!");
                return ResponseEntity.ok(flights);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("항공편 api를 요청하고 저장하는 과정에서 오류가 발생했습니다." + e);
        }
    }
}