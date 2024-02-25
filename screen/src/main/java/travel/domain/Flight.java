package travel.domain;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import travel.ScreenApplication;

@Entity
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("airlineNm")
    private String airLine;

    @JsonProperty("arrAirportNm")
    private String arrAirport;

    @JsonProperty("depAirportNm")
    private String depAirport;

    @JsonProperty("arrPlandTime")
    private Long arrTime;

    @JsonProperty("depPlandTime")
    private Long depTime;

    private Long economyCharge = 57900L;

    private Long prestigeCharge = 87900L;

    private String vihicleId;

    private Long seatCapacity = 100L;

    public static FlightRepository repository() {
        FlightRepository flightRepository = ScreenApplication.applicationContext.getBean(
                FlightRepository.class);
        return flightRepository;
    }
}
