package travel.repository;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import travel.domain.Room;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "rooms", path = "rooms")
public interface RoomRepository
    extends PagingAndSortingRepository<Room, Long> {

    Optional<Room>findByContentid(Long contentid) ;
}
