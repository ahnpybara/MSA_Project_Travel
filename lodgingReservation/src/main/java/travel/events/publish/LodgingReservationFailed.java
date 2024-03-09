package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationFailed extends AbstractEvent {

    private Long id;

    public LodgingReservationFailed(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationFailed() {
        super();
    }
}
