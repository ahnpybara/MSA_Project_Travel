package travel.events.subscribe;

import lombok.Data;
import lombok.ToString;
import travel.events.publish.AbstractEvent;

@Data
@ToString
public class LodgingReservationCancelRequested extends AbstractEvent {

    private Long id;
    private String name;
    private Long reservationDate;
    private String email;
    private String category;
    private Long charge;
    private Long roomCode;
    private String status;
}
