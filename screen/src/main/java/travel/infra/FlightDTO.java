package travel.infra;

import lombok.Data;

@Data
public class FlightDTO {
    String depAirport;
    String arrAirport;
    String depTime;
}
