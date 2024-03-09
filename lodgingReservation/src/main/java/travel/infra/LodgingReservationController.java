package travel.infra;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.*;

@RestController
@RequestMapping(value="/lodgingReservations")
@Transactional
public class LodgingReservationController {

    @Autowired
    LodgingReservationRepository lodgingReservationRepository;
}
