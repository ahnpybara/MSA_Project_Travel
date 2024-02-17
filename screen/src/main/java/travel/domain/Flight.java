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

    @PostPersist
    public void onPostPersist() {
        FlightBookRequested flightBookRequested = new FlightBookRequested(this);
        flightBookRequested.publishAfterCommit();
    }

    public static FlightRepository repository() {
        FlightRepository flightRepository = ScreenApplication.applicationContext.getBean(
            FlightRepository.class
        );
        return flightRepository;
    }

    //<<< Clean Arch / Port Method
    public static void reservationStatus(PaymentRequested paymentRequested) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(paymentRequested.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void reservationCancellationStatus(
        PaymentCnlRequested paymentCnlRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(paymentCnlRequested.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
