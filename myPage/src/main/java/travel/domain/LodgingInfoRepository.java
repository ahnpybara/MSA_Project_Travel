package travel.domain;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "lodgingInfos", path = "lodgingInfos")
public interface LodgingInfoRepository extends PagingAndSortingRepository<LodgingInfo, Long> {
    List<LodgingInfo> findByUserId(Long userId);
    LodgingInfo findByReservationId(Long id);
}
