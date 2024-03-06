package travel.domain;

import java.util.*;
import lombok.*;
import travel.domain.*;
import travel.infra.AbstractEvent;

@Data
@ToString
public class PaymentFailed extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private Long reservationId;
    private String name;
    private String impUid;
    private String status;
}
