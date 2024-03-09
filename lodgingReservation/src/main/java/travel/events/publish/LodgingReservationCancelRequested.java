package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationCancelRequested extends AbstractEvent {

    private Long id;

    public LodgingReservationCancelRequested(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationCancelRequested() {
        super();
    }
}
