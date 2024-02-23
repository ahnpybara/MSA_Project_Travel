package travel.domain;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import travel.ScreenApplication;

@Entity
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("airlineNm")
    private String airLine;

    @JsonProperty("arrAirportNm")
    private String arrAirport;

    @JsonProperty("depAirportNm")
    private String depAirport;

    @JsonProperty("arrPlandTime")
    private Long arrTime;

    @JsonProperty("depPlandTime")
    private Long depTime;

    private Long economyCharge = 57900L;

    private Long prestigeCharge = 87900L;

    private String vihicleId;

    private Long seatCapacity = 100L;

    public static FlightRepository repository() {
        FlightRepository flightRepository = ScreenApplication.applicationContext.getBean(
                FlightRepository.class);
        return flightRepository;
    }

    public static void reservationStatus(PaymentRequested paymentRequested) {

        repository().findById(paymentRequested.getFlightId()).ifPresent(flight -> {
            if (flight.seatCapacity <= 0) throw new IllegalArgumentException("No more seats available");
            flight.setSeatCapacity(flight.seatCapacity - 1);
            repository().save(flight);
        });
    }

    public static void reservationCancellationStatus(FlightbookCancelled flightbookCancelled) {

        repository().findById(flightbookCancelled.getFlightId()).ifPresent(flight -> {
            if (flight.seatCapacity >= 100) throw new IllegalArgumentException("Seat capacity exceeded");
            flight.setSeatCapacity(flight.seatCapacity - 1);
            repository().save(flight);
        });
    }
}
