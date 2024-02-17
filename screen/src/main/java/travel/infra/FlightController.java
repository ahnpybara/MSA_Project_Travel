package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/flights")
@Transactional
public class FlightController {

    @Autowired
    FlightRepository flightRepository;

    @PostMapping("/search")
    public ResponseEntity<String> search(){
        return ResponseEntity.ok("ok");
    }
}
//>>> Clean Arch / Inbound Adaptor
