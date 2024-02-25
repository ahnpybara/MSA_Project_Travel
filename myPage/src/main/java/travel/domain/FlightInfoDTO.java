package travel.domain;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FlightInfoDTO {
    @NotNull(message = "userId cannot be null")
    private Long userId;
}
