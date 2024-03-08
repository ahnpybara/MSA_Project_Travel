package travel.event.subscribe;

import lombok.*;
import travel.event.publish.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightReservationRequested extends AbstractEvent {

    private Long id;
    private Long flightId;
    private String email;
}
