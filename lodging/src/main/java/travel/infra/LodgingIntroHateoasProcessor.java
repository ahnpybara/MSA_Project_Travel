package travel.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import travel.domain.LodgingIntro;

@Component
public class LodgingIntroHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<LodgingIntro>> {

    @Override
    public EntityModel<LodgingIntro> process(EntityModel<LodgingIntro> model) {
        return model;
    }
}
