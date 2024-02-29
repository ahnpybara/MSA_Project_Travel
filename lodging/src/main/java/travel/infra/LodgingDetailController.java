package travel.infra;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.*;


@RestController
@Transactional
public class LodgingDetailController {

    @Autowired
    LodgingDetailRepository lodgingDetailRepository;
}
