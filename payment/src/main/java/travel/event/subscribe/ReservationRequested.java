package travel.event.subscribe;

import lombok.*;
import travel.event.publish.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class ReservationRequested extends AbstractEvent {

    private Long id;
    private Long charge;
    private Long userId;
    private String name;
    private Long flightId;
    private String email;
    private String category;
}
