package travel.events.publish;

import lombok.*;
import travel.domain.*;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightReservationFailed extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Long arrTime;
    private Long depTime;
    private Long charge;
    private String vihicleId;
    private Status status;
    private Long userId;
    private String name;
    private Long flightId;
    private String email;
    private String reservationHash;
    private String category;


    public FlightReservationFailed(FlightReservation aggregate) {
        super(aggregate);
    }

    public FlightReservationFailed() {
        super();
    }
}
