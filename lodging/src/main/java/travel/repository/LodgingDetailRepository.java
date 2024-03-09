package travel.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import travel.domain.LodgingDetail;

@RepositoryRestResource(collectionResourceRel = "lodgingDetails", path = "lodgingDetails")
public interface LodgingDetailRepository extends PagingAndSortingRepository<LodgingDetail, Long> {
}
