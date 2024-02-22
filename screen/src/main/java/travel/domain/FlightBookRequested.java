package travel.domain;

import java.util.*;
import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class FlightBookRequested extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Date arrTime;
    private Date depTime;
    private Long charge;
    private String vihicleId;
    private Long seatCapacity;

    public FlightBookRequested(Flight aggregate) {
        super(aggregate);
    }

    public FlightBookRequested() {
        super();
    }
}
