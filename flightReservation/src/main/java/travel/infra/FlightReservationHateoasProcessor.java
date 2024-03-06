package travel.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import travel.domain.*;

@Component
public class FlightReservationHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<FlightReservation>> {

    @Override
    public EntityModel<FlightReservation> process(
        EntityModel<FlightReservation> model
    ) {
        return model;
    }
}
