package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.FlightReservationApplication;


@Entity
@Table(name = "FlightReservation_table")
@Data
public class FlightReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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


    public static FlightReservationRepository repository() {
        FlightReservationRepository flightReservationRepository = FlightReservationApplication.applicationContext.getBean(
            FlightReservationRepository.class);
        return flightReservationRepository;
    }
}