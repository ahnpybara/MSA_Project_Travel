package travel.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import travel.domain.entity.LodgingDetail;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "lodgingDetails",
    path = "lodgingDetails"
)
public interface LodgingDetailRepository
    extends PagingAndSortingRepository<LodgingDetail, Long> {

    Optional<LodgingDetail> findByContentid(Long contentid);}
