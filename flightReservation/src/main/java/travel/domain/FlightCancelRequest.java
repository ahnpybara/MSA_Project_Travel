package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightCancelRequest extends AbstractEvent{
    
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
    private String reservationHash;

    public FlightCancelRequest(FlightReservation aggregate) {
        super(aggregate);
    }

    public FlightCancelRequest() {
        super();
    }
}