package travel.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
@Data
public class FlightDTO {
    @NotBlank(message = "depAirport cannot be null or empty")
    private String depAirport;

    @NotBlank(message = "arrAirport cannot be null or empty")
    private String arrAirport;

    @NotBlank(message = "depTime cannot be null or empty")
    private String depTime;
}