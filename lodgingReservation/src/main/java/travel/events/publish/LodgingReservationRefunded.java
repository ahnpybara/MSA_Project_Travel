package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationRefunded extends AbstractEvent {

    private Long id;

    public LodgingReservationRefunded(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationRefunded() {
        super();
    }
}
