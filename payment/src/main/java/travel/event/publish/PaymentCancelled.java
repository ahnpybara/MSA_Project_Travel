package travel.event.publish;

import lombok.*;
import travel.domain.*;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentCancelled extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;

    public PaymentCancelled(Payment aggregate) {
        super(aggregate);
    }

    public PaymentCancelled() {
        super();
    }
}
