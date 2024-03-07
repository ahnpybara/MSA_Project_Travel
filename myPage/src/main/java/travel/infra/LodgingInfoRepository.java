package travel.infra;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

@RepositoryRestResource(
    collectionResourceRel = "lodgingInfos",
    path = "lodgingInfos"
)
public interface LodgingInfoRepository
    extends PagingAndSortingRepository<LodgingInfo, Long> {}
