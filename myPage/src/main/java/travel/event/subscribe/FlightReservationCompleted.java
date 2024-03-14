package travel.event.subscribe;

import lombok.Data;
import lombok.EqualsAndHashCode;
import travel.event.publish.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class FlightReservationCompleted extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Long arrTime;
    private Long depTime;
    private Long charge;
    private String vihicleId;
    private Long userId;
    private String name;
    private Long flightId;
    private String email;
    private String category;
}
