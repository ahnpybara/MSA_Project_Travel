package travel.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

@RepositoryRestResource(collectionResourceRel = "lodgingIntros", path = "lodgingIntros")
public interface LodgingIntroRepository
        extends PagingAndSortingRepository<LodgingIntro, Long> {
}
