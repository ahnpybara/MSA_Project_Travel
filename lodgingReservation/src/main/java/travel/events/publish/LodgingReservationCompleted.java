package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationCompleted extends AbstractEvent {

    private Long id;

    public LodgingReservationCompleted(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationCompleted() {
        super();
    }
}
