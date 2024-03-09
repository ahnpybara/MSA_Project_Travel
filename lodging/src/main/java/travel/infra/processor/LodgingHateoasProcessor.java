package travel.infra.processor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import travel.domain.entity.Lodging;

@Component
public class LodgingHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Lodging>> {

    @Override
    public EntityModel<Lodging> process(EntityModel<Lodging> model) {
        return model;
    }
}
