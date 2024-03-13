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

    private String name;

    private Long reservationDate;

    private String email;

    private String category;

    private Long charge;

    private Long roomCode;

    private Status status;



    public static LodgingReservationRepository repository() {
        LodgingReservationRepository lodgingReservationRepository = LodgingReservationApplication.applicationContext.getBean(LodgingReservationRepository.class);
        return lodgingReservationRepository;
    }
}
