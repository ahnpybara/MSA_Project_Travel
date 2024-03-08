package travel.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "lodgingInfos", path = "lodgingInfos")
public interface LodgingInfoRepository extends PagingAndSortingRepository<LodgingInfo, Long> {
}
