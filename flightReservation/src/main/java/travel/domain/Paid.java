package travel.domain;

import java.util.*;
import lombok.*;
import travel.domain.*;
import travel.infra.AbstractEvent;

@Data
@ToString
public class Paid extends AbstractEvent {
    private Long id;

    private Long reservationId;

    private Long charge;

    private Long userId;

    private String name;

    private String imp_uid;
}
