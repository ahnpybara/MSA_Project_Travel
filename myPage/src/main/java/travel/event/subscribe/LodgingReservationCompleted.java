package travel.event.subscribe;

import lombok.Data;
import lombok.EqualsAndHashCode;
import travel.event.publish.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class LodgingReservationCompleted extends AbstractEvent {

    private Long id;
    private Long userId;
    private String name;
    private Long reservationDate;
    private String email;
    private String category;
    private Long charge;
    private Long roomCode;
}
