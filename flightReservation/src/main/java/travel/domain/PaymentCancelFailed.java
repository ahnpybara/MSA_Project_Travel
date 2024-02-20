package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@ToString
public class PaymentCancelFailed extends AbstractEvent {

    private Long reservationId;
    
}
