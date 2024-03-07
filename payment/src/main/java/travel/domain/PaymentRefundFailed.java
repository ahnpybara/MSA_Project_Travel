package travel.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import travel.domain.*;
import travel.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
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
//>>> DDD / Domain Event
