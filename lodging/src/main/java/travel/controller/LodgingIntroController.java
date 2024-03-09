package travel.controller;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.repository.LodgingIntroRepository;


@RestController
@RequestMapping(value="/lodgingIntros")
@Transactional
public class LodgingIntroController {

    @Autowired
    LodgingIntroRepository lodgingIntroRepository;
}
