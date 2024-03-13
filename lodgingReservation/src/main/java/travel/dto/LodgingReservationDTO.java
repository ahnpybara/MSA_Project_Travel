package travel.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
@Data
public class LodgingReservationDTO {
    
    private Long id;
    @NotBlank(message = "\nname cannot be null or empty\n")
    private String name;
    @NotBlank(message = "\nreservationDate cannot be null or empty\n")
    private Long reservationDate;
    @NotBlank(message = "\nemail cannot be null or empty\n")
    private String email;
    @NotNull(message = "\ncharge cannot be null or empty\n")
    private Long charge;
    @NotNull(message = "\nroomCode cannot be null or empty\n")
    private Long roomCode;

    @NotNull(message = "\nflightId cannot be null or empty\n")
    private Long flightId;

}
