package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationCancelled extends AbstractEvent {

    private Long id;

    public LodgingReservationCancelled(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationCancelled() {
        super();
    }
}
