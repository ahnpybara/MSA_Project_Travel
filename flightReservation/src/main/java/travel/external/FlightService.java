package travel.external;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "screen", url = "${api.url.screen}")
public interface FlightService {
    @GetMapping(path = "/flights")
    public List<Flight> getFlight();

    @GetMapping(path = "/flights/{id}")
    public Flight getFlight(@PathVariable("id") Long id);
    //경로 확인해 볼것
    @GetMapping(path = "/flights/search")
    public ResponseEntity<Flight> searchFlights(@RequestParam("flightId") Long flightId);                            

}
