package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightCancelRequested extends AbstractEvent{
    
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

    public FlightCancelRequested(FlightReservation aggregate) {
        super(aggregate);
    }

    public FlightCancelRequested() {
        super();
    }
}