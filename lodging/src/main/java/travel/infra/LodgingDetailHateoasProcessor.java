package travel.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import travel.domain.*;

@Component
public class LodgingDetailHateoasProcessor implements RepresentationModelProcessor<EntityModel<LodgingDetail>> {

    @Override
    public EntityModel<LodgingDetail> process(EntityModel<LodgingDetail> model) {
        return model;
    }
}
