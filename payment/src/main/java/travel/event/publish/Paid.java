package travel.event.publish;

import lombok.*;
import travel.domain.*;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class Paid extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;

    public Paid(Payment aggregate) {
        super(aggregate);
    }

    public Paid() {
        super();
    }
}
