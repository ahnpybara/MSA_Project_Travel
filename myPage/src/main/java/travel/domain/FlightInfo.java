package travel.domain;

import java.util.Date;
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
    private Long userId;
    private String name;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Date arrTime;
    private Date depTime;
    private Long charge;
    private String vihicleId;
    private FlightStatus status;
}
