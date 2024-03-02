package travel.domain;


import lombok.*;
import travel.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightbookCancelled extends AbstractEvent {
    
    private Long id;
    private Long flightId;
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
    private String email;
    private String reservationHash;

    public FlightbookCancelled(FlightReservation aggregate) {
        super(aggregate);
    }

    public FlightbookCancelled() {
        super();
    }
}
//>>> DDD / Domain Event
