package travel.events.publish;


import lombok.*;
import travel.domain.*;

//<<< DDD / Domain Event
@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightReservationCompleted extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Long arrTime;
    private Long depTime;
    private Long charge;
    private String vihicleId;
    private String status;
    private Long userId;
    private String name;
    private Long flightId;
    private String email;
    private String reservationHash;

    public FlightReservationCompleted(FlightReservation aggregate) {
        super(aggregate);
    }

    public FlightReservationCompleted() {
        super();
    }
}

