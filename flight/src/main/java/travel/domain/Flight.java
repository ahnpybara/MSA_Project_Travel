package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.FlightApplication;

@Entity
@Table(name = "Flight_table")
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String airlineNm;

    private String arrAirportNm;

    private String depAirportNm;

    private Long arrPlandTime;

    private Long depPlandTime;

    private Long economyCharge;

    private Long prestigeCharge;

    private String vihicleId;

    private Long seatCapacity = 100L;

    public static FlightRepository repository() {
        FlightRepository flightRepository = FlightApplication.applicationContext.getBean(FlightRepository.class);
        return flightRepository;
    }
}
