package travel.repository;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import travel.domain.LodgingIntro;


//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "lodgingIntros",
    path = "lodgingIntros"
)
public interface LodgingIntroRepository
    extends PagingAndSortingRepository<LodgingIntro, Long> {

        Optional<LodgingIntro> findByContentid(Long contentid);
    }