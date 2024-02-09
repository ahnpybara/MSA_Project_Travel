package travel.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import travel.domain.*;

@Component
public class FlightHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Flight>> {

    @Override
    public EntityModel<Flight> process(EntityModel<Flight> model) {
        return model;
    }
}
