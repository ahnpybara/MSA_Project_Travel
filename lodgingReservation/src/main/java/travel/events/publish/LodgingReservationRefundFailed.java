package travel.events.publish;

import lombok.*;
import travel.domain.LodgingReservation;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LodgingReservationRefundFailed extends AbstractEvent {

    private Long id;

    public LodgingReservationRefundFailed(LodgingReservation aggregate) {
        super(aggregate);
    }

    public LodgingReservationRefundFailed() {
        super();
    }
}
