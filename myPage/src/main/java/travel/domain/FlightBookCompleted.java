package travel.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;
import travel.infra.AbstractEvent;

@Data
public class FlightBookCompleted extends AbstractEvent {

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
    private String passenger;
}