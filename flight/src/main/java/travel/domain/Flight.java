package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.FlightApplication;

@Entity
@Table(name = "Flight_table")
@Data
//<<< DDD / Aggregate Root
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String airLine;

    private String arrAirport;

    private String depAirport;

    private Date arrTime;

    private Date depTime;

    private Long charge;

    private String vihicleId;

    private Long seatCapacity;

    @PostPersist
    public void onPostPersist() {}

    public static FlightRepository repository() {
        FlightRepository flightRepository = FlightApplication.applicationContext.getBean(
            FlightRepository.class
        );
        return flightRepository;
    }

    //<<< Clean Arch / Port Method
    public static void increaseSeatCapcity(
        FlightReservaionCancelRequested flightReservaionCancelRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservaionCancelRequested.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void increaseSeatCapcity(
        FlightReservationRefunded flightReservationRefunded
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservationRefunded.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void increaseSeatCapcity(
        FlightReservaionCancelled flightReservaionCancelled
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservaionCancelled.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void increaseSeatCapcity(
        FlightReservaionFailed flightReservaionFailed
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservaionFailed.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void decreaseSeatCapcity(
        FlightReservaionRequested flightReservaionRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Flight flight = new Flight();
        repository().save(flight);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservaionRequested.get???()).ifPresent(flight->{
            
            flight // do something
            repository().save(flight);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
