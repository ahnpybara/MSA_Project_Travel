package travel.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CancelReservationDTO {

    @NotNull(message = "\n lodgingReservationId cannot be null or empty\n")
    private Long lodgingReservationId; 

}
