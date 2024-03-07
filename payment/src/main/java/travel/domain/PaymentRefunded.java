package travel.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import travel.domain.*;
import travel.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class PaymentRefunded extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;
    private String status;

    public PaymentRefunded(Payment aggregate) {
        super(aggregate);
    }

    public PaymentRefunded() {
        super();
    }
}
//>>> DDD / Domain Event
