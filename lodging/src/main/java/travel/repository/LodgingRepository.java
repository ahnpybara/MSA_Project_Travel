package travel.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import travel.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "lodgings", path = "lodgings")
public interface LodgingRepository
        extends PagingAndSortingRepository<Lodging, Long> {
    List<Lodging> findAllByAreaCode(long areaCode);
}
