package travel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "lodgingDetails",
    path = "lodgingDetails"
)
public interface LodgingDetailRepository
    extends PagingAndSortingRepository<LodgingDetail, Long> {

    Optional<LodgingDetail> findByContentId(Long contentId);}
