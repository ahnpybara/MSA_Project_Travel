package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentRequested extends AbstractEvent {

    private Long id;

    private Long charge;

    private Long userId;

    private String name;
}
