package travel.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightReservationDTO {
    
    private Long id;
    @NotBlank(message = "\nairLine cannot be null or empty\n")
    private String airLine;
    @NotBlank(message = "\narrAirport cannot be null or empty\n")
    private String arrAirport;
    @NotBlank(message = "\ndepAirport cannot be null or empty\n")
    private String depAirport;
    @NotNull(message = "\narrTime cannot be null or empty\n")
    private Long arrTime;
    @NotNull(message = "\ndepTime cannot be null or empty\n")
    private Long depTime;
    @NotNull(message = "\ncharge cannot be null or empty\n")
    private Long charge;
    @NotBlank(message = "\nvihicleId cannot be null or empty\n")
    private String vihicleId;
    @NotNull(message = "\nuserId cannot be null or empty\n")
    private Long userId;
    @NotBlank(message = "\nname cannot be null or empty\n")
    private String name;
    @NotBlank(message = "\nemail cannot be null or empty\n")
    private String email;
    @NotNull(message = "\nflightId cannot be null or empty\n")
    private Long flightId;

}
