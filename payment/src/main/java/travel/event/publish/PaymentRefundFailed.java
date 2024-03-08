package travel.event.publish;

import lombok.*;
import travel.domain.*;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentRefundFailed extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;
    private String status;

    public PaymentRefundFailed(Payment aggregate) {
        super(aggregate);
    }

    public PaymentRefundFailed() {
        super();
    }
}
