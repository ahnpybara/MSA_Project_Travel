package travel.domain;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightInfoDTO {
    @NotNull(message = "depAirport cannot be null")
    private Long userId;
}
