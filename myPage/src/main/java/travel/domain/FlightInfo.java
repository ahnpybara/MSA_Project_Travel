package travel.domain;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "FlightInfo_table")
@Data
public class FlightInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)

    private Long id;
    private Long reservationId;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Long arrTime;
    private Long depTime;
    private Long charge;
    private String vihicleId;
    private Long userId;
    private String name;
    private Long flightId;
    private String email;
    private String category;
    private FlightStatus status;
}
