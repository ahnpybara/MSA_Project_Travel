package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;
import travel.domain.Status;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationCancelled extends AbstractEvent {

    private Long id;
    private String name;
    private Long reservationDate;
    private String email;
    private String category;
    private Long charge;
    private Long roomCode;
    private Status status;

    public LodgingReservationCancelled(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationCancelled() {
        super();
    }
}
