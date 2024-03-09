package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.LodgingReservationApplication;

@Entity
@Table(name = "LodgingReservation_table")
@Data
public class LodgingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public static LodgingReservationRepository repository() {
        LodgingReservationRepository lodgingReservationRepository = LodgingReservationApplication.applicationContext
                .getBean(LodgingReservationRepository.class);
        return lodgingReservationRepository;
    }
}
