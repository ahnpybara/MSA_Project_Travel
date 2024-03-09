package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationRequested extends AbstractEvent {

    private Long id;

    public LodgingReservationRequested(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationRequested() {
        super();
    }
}
