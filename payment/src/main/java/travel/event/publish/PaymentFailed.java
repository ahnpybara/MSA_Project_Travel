package travel.event.publish;

import lombok.*;
import travel.domain.*;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentFailed extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;
    private String status;

    public PaymentFailed(Payment aggregate) {
        super(aggregate);
    }

    public PaymentFailed() {
        super();
    }
}
