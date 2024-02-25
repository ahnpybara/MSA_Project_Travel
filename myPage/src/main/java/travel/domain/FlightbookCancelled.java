package travel.domain;

import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class FlightbookCancelled extends AbstractEvent {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Date arrTime;
    private Date depTime;
    private Long charge;
    private String vihicleId;
    private String status;
    private Long userId;
    private String name;
    private String email;
}
