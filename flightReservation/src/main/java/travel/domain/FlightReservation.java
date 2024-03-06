package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.FlightReservationApplication;
import travel.domain.FlightReservaionCancelRequested;
import travel.domain.FlightReservaionCancelled;
import travel.domain.FlightReservaionFailed;
import travel.domain.FlightReservaionRequested;
import travel.domain.FlightReservationCompleted;
import travel.domain.FlightReservationRefunded;
import travel.domain.PaymentRefundFailed;

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

    private Long flightId;

    private String email;

    @PostPersist
    public void onPostPersist() {
        FlightReservaionRequested flightReservaionRequested = new FlightReservaionRequested(
            this
        );
        flightReservaionRequested.publishAfterCommit();

        FlightReservationCompleted flightReservationCompleted = new FlightReservationCompleted(
            this
        );
        flightReservationCompleted.publishAfterCommit();

        FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(
            this
        );
        flightReservationRefunded.publishAfterCommit();

        PaymentRefundFailed paymentRefundFailed = new PaymentRefundFailed(this);
        paymentRefundFailed.publishAfterCommit();

        FlightReservaionCancelRequested flightReservaionCancelRequested = new FlightReservaionCancelRequested(
            this
        );
        flightReservaionCancelRequested.publishAfterCommit();

        FlightReservaionFailed flightReservaionFailed = new FlightReservaionFailed(
            this
        );
        flightReservaionFailed.publishAfterCommit();

        FlightReservaionCancelled flightReservaionCancelled = new FlightReservaionCancelled(
            this
        );
        flightReservaionCancelled.publishAfterCommit();
        // Get request from FlightReservation
        //travel.external.FlightReservation flightReservation =
        //    Application.applicationContext.getBean(travel.external.FlightReservationService.class)
        //    .getFlightReservation(/** mapping value needed */);

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

        FlightReservationCompleted flightReservationCompleted = new FlightReservationCompleted(flightReservation);
        flightReservationCompleted.publishAfterCommit();
        FlightReservationCompleted flightReservationCompleted = new FlightReservationCompleted(flightReservation);
        flightReservationCompleted.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paid.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightReservationCompleted flightReservationCompleted = new FlightReservationCompleted(flightReservation);
            flightReservationCompleted.publishAfterCommit();
            FlightReservationCompleted flightReservationCompleted = new FlightReservationCompleted(flightReservation);
            flightReservationCompleted.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void paymentRefund(PaymentRefunded paymentRefunded) {
        //implement business logic here:

        /** Example 1:  new item 
        FlightReservation flightReservation = new FlightReservation();
        repository().save(flightReservation);

        FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(flightReservation);
        flightReservationRefunded.publishAfterCommit();
        FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(flightReservation);
        flightReservationRefunded.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paymentRefunded.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(flightReservation);
            flightReservationRefunded.publishAfterCommit();
            FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(flightReservation);
            flightReservationRefunded.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void paymentRefundFail(
        PaymentRefundFailed paymentRefundFailed
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        FlightReservation flightReservation = new FlightReservation();
        repository().save(flightReservation);

        PaymentRefundFailed paymentRefundFailed = new PaymentRefundFailed(flightReservation);
        paymentRefundFailed.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paymentRefundFailed.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            PaymentRefundFailed paymentRefundFailed = new PaymentRefundFailed(flightReservation);
            paymentRefundFailed.publishAfterCommit();

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

        FlightReservaionCancelled flightReservaionCancelled = new FlightReservaionCancelled(flightReservation);
        flightReservaionCancelled.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paymentCancelled.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightReservaionCancelled flightReservaionCancelled = new FlightReservaionCancelled(flightReservation);
            flightReservaionCancelled.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void paymentFailed(PaymentFailed paymentFailed) {
        //implement business logic here:

        /** Example 1:  new item 
        FlightReservation flightReservation = new FlightReservation();
        repository().save(flightReservation);

        FlightReservaionFailed flightReservaionFailed = new FlightReservaionFailed(flightReservation);
        flightReservaionFailed.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(paymentFailed.get???()).ifPresent(flightReservation->{
            
            flightReservation // do something
            repository().save(flightReservation);

            FlightReservaionFailed flightReservaionFailed = new FlightReservaionFailed(flightReservation);
            flightReservaionFailed.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
