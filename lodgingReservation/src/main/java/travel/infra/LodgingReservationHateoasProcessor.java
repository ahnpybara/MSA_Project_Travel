package travel.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import travel.domain.*;

@Component
public class LodgingReservationHateoasProcessor
        implements RepresentationModelProcessor<EntityModel<LodgingReservation>> {

    @Override
    public EntityModel<LodgingReservation> process(
            EntityModel<LodgingReservation> model) {
        return model;
    }
}
