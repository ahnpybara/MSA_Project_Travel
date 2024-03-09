package travel.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CancelReservationDTO {
    

    @NotNull(message = "\n flightReservationId cannot be null or empty\n")
    private Long flightReservationId;

}
