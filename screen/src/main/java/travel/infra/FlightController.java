package travel.infra;

import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<?> getAndSaveFlightData(@Valid @RequestBody FlightDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터들이 NULL인지 EMPTY인지 검증을 수행합니다
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body("Invalid request parameters");
        }

        // 사용자로 부터 받은 DTO(항공편 API 요청 정보) 객체에서 각 데이터를 꺼내서 변수에 저장
        String depAirportId = request.getDepAirport();
        String arrAirportId = request.getArrAirport();
        String depPlandTime = request.getDepTime();

        // DB에는 년/월/일/시/분 단위로 저장되지만, 전달되는 데이터는 년/월/일 단위로 전달되기 때문에 DB 조회를 위해서 형식을 맞춤
        Long startTimestamp = Long.parseLong(depPlandTime + "0000");
        Long endTimestamp = Long.parseLong(depPlandTime + "2359");

        try {
            // 출발 공항 ID, 도착 공항 ID, 출발 시간, 도착 시간을 조건으로 하는 항공편을 찾습니다.
            List<Flight> flights = flightService.findFlights(depAirportId, arrAirportId, startTimestamp, endTimestamp);

            // 만약 사용자가 요청한 항공편 정보가 없다면 API를 호출하고, 데이터베이스에 저장한 뒤, 저장된 항공편 정보를 반환합니다.
            if (flights.isEmpty()) { 
                // 사용자로 부터 전달된 도착공항, 출발공항, 출발날짜를 이용해서 항공편 API를 호출합니다
                String flightData = flightService.callApi(depAirportId, arrAirportId, depPlandTime);

                if (flightData == null || flightData.isEmpty()) // API로 부터 받은 정보가 아무것도 없을 경우 예외발생
                    throw new Exception("항공편 데이터를 가져오는 데 실패했습니다.");

                System.out.println("++++++++++++++++++++++++++++저장로직 실행!!");
                List<Flight> savedFlights = flightService.saveFlightData(flightData); // 항공편 API로 부터  받은 정보를 DB에 저장합니다

                return ResponseEntity.ok(savedFlights); // 저장된 정보를 사용자에게 응답으로 줍니다
            } else { // 사용자가 요청한 항공편 정보가 이미 DB에 있다면 해당 정보를 바로 응답으로 줍니다
                System.out.println("++++++++++++++++++++++++++++조회로직 실행!!");
                return ResponseEntity.ok(flights); 
            }
        } catch (RollBackException e) { // 서비스 계층에서 발생한 롤백이 수행되어지는 예외를 잡습니다
            throw new RuntimeException(e); // 스프링에게 RuntimeException을 전달합니다 -> 롤백수행
        } catch (Exception e) { // 서비스 계층에서 발생한 롤백과 관련없는 예외들은 컨트롤러에서 잡아서 처리합니다 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("항공편 api를 요청하고 저장하는 과정에서 오류가 발생했습니다." + e);
        }
    }
}