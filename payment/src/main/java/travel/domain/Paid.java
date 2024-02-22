package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class Paid extends AbstractEvent {

    private Long id;

    private Long reservationId;

    private Long charge;

    private Long userId;

    private String name;

    private String imp_uid;
    
    public Paid(Payment aggregate) {
        super(aggregate);
    }

    public Paid() {
        super();
    }
}
