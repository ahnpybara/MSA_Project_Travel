package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentFailed extends AbstractEvent {
    
    private Long reservationId;

    public PaymentFailed(Payment aggregate) {
        super(aggregate);
    }

    public PaymentFailed() {
        super();
    }
}