package travel.external;

import java.util.Date;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "flight", url = "${api.url.flight}")
public interface FlightService {
    @GetMapping(path = "/flights")
    public List<Flight> getFlight();

    @GetMapping(path = "/flights/{id}")
    public Flight getFlight(@PathVariable("id") Long id);
}
