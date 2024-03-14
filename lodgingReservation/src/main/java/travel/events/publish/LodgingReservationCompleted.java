package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;
import travel.domain.Status;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationCompleted extends AbstractEvent {

    private Long id;
    private String name;
    private Long reservationDate;
    private String email;
    private String category;
    private Long charge;
    private Long roomcode;
    private Status status;
    private Long userId;

    public LodgingReservationCompleted(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationCompleted() {
        super();
    }
}
