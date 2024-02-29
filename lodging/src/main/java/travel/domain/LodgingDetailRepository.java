package travel.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "lodgingDetails",
    path = "lodgingDetails"
)
public interface LodgingDetailRepository
    extends PagingAndSortingRepository<LodgingDetail, Long> {}