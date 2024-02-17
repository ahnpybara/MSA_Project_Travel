package travel.infra;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

@RepositoryRestResource(
    collectionResourceRel = "flightInfos",
    path = "flightInfos"
)
public interface FlightInfoRepository
    extends PagingAndSortingRepository<FlightInfo, Long> {
    List<FlightInfo> findByUserId(Long userId);
}
