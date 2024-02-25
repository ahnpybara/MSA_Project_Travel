package travel.external;


import lombok.Data;

@Data
public class Flight {

    private Long id;
    private String airLine;
    private String arrAirport;
    private String depAirport;
    private Long arrTime;
    private Long depTime;
    private Long charge;
    private String vihicleId;
    private Long seatCapacity;
}
