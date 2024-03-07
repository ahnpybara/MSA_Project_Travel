package travel.event.subscribe;

import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import travel.event.publish.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class FlightReservationRefunded extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Date arrTime;
    private Date depTime;
    private Long charge;
    private String vihicleId;
    private Long userId;
    private String name;
    private Long FlightId;
    private String email;
}
