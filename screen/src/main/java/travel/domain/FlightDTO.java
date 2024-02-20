package travel.domain;

import lombok.Data;

@Data
public class FlightDTO {
    private String depAirport;
    private String arrAirport;
    private String depTime;
}
