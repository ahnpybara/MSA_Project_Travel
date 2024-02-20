package travel.external;

import java.util.Date;
import lombok.Data;

@Data
public class Flight {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Date arrTime;
    private Date depTime;
    private Long charge;
    private String vihicleId;
    private Long seatCapacity;
}
