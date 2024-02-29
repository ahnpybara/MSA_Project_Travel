package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentCancelFailed extends AbstractEvent {

    private Long reservationId;
    
}
