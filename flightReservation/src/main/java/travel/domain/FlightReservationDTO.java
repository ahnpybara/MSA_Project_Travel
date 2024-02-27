package travel.domain;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightReservationDTO {
    
    private Long id;
    @NotBlank(message = "airLine cannot be null or empty")
    private String airLine;
    @NotBlank(message = "arrAirport cannot be null or empty")
    private String arrAirport;
    @NotBlank(message = "depAirport cannot be null or empty")
    private String depAirport;
    @NotNull(message = "arrTime cannot be null or empty")
    private Long arrTime;
    @NotNull(message = "depTime cannot be null or empty")
    private Long depTime;
    @NotNull(message = "charge cannot be null or empty")
    private Long charge;
    @NotBlank(message = "vihicleId cannot be null or empty")
    private String vihicleId;
    @NotNull(message = "userId cannot be null or empty")
    private Long userId;
    @NotBlank(message = "name cannot be null or empty")
    private String name;
    @NotNull(message = "flightId cannot be null or empty")
    private Long flightId;
}
