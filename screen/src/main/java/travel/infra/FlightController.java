package travel.infra;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import travel.domain.*;

@RestController
@RequestMapping(value="/flights")
@Transactional
public class FlightController {

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    private FlightService flightService;

    @GetMapping
    public String getAndSaveFlightData(@RequestParam String depAirportId, @RequestParam String arrAirportId,
            @RequestParam String depPlandTime, @RequestParam String airlineId) {
        return flightService.callApi(depAirportId, arrAirportId, depPlandTime, airlineId);
    }
    
}