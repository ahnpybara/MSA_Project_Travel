package travel.domain;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LodgingInfo_table")
@Data
public class LodgingInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Long userId;
    private Long reservationId;
    private Long roomCode;
    private String email;
    private String category;
    private ReservationStatus status;
    private Long charge;
    private Long reservationDate;
}
