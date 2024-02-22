package travel.domain;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "payments", path = "payments")
public interface PaymentRepository
        extends PagingAndSortingRepository<Payment, Long> {

    Optional<Payment> findByReservationId(Long reservationId);
}
