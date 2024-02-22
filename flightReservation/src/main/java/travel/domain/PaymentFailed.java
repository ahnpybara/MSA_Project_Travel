package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@ToString
public class PaymentFailed extends AbstractEvent{
    
    private Long reservationId;

}
