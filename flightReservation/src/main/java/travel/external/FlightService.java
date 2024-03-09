package travel.external;


import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "flight", url = "${api.url.flight}")// 나중에 리펙토링 제대로 되면 screen 이아니라 flight 로 사용 yml에도 screen->flight 변경
public interface FlightService {
    @GetMapping(path = "/flights")
    public List<Flight> getFlight();

    @GetMapping(path = "/flights/{id}")
    public Flight getFlight(@PathVariable("id") Long id);

    @GetMapping(path = "/flights/seatCapacity")       // 좌석 호출 하기 위해 찾음
    public ResponseEntity<Flight> searchFlights(@RequestParam("flightId") Long flightId);     
}
