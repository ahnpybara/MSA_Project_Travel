package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.FlightReservationApplication;
import travel.domain.FlightBookCompleted;
import travel.domain.FlightbookCancelled;
import travel.domain.PaymentCnlRequested;
import travel.domain.PaymentRequested;

@Entity
@Table(name = "FlightReservation_table")
@Data
//<<< DDD / Aggregate Root
public class FlightReservation {

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

    private String status;

    private Long userId;

    private String name;

    @PostPersist
    public void onPostPersist() {
        PaymentRequested paymentRequested = new PaymentRequested(this);
        paymentRequested.publishAfterCommit();

        // PaymentCnlRequested paymentCnlRequested = new PaymentCnlRequested(this);
        // paymentCnlRequested.publishAfterCommit();

        // FlightBookCompleted flightBookCompleted = new FlightBookCompleted(this);
        // flightBookCompleted.publishAfterCommit();

        // FlightbookCancelled flightbookCancelled = new FlightbookCancelled(this);
        // flightbookCancelled.publishAfterCommit();
        // Get request from FlightReservation
        //travel.external.FlightReservation flightReservation =
        //    Application.applicationContext.getBean(travel.external.FlightReservationService.class)
        //    .getFlightReservation(/** mapping value needed */);

    }

    public void reserve(FlightBookCompleted flightBookCompleted){
        setAirLine(flightBookCompleted.getAirLine());
        setArrAirport(flightBookCompleted.getArrAirport());
        setDepAirport(flightBookCompleted.getDepAirport());
        setArrTime(flightBookCompleted.getArrTime());
        setDepTime(flightBookCompleted.getDepTime());
        setCharge(flightBookCompleted.getCharge());
        setVihicleId(flightBookCompleted.getVihicleId());
    }

    public static FlightReservationRepository repository() {
        FlightReservationRepository flightReservationRepository = FlightReservationApplication.applicationContext.getBean(
            FlightReservationRepository.class
        );
        return flightReservationRepository;
    }

    //<<< Clean Arch / Port Method
    public static void paymentComplete(Paid paid) {
        //implement business logic here:

        /** Example 1:  new item 
        FlightReservation flightReservation = new FlightReservation();
        repository().save(flightReservation);

        FlightBookCompleted flightBookCompleted = new FlightBookCompleted(flightReservation);
        flightBookCompleted.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paid.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightBookCompleted flightBookCompleted = new FlightBookCompleted(flightReservation);
            flightBookCompleted.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void paymentCancel(PaymentCancelled paymentCancelled) {
        //implement business logic here:

        /** Example 1:  new item 
        FlightReservation flightReservation = new FlightReservation();
        repository().save(flightReservation);

        FlightbookCancelled flightbookCancelled = new FlightbookCancelled(flightReservation);
        flightbookCancelled.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paymentCancelled.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightbookCancelled flightbookCancelled = new FlightbookCancelled(flightReservation);
            flightbookCancelled.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root