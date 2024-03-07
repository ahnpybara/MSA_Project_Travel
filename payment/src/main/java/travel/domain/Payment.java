package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.PaymentApplication;
import travel.domain.Paid;
import travel.domain.PaymentCancelled;
import travel.domain.PaymentFailed;
import travel.domain.PaymentRefundFailed;
import travel.domain.PaymentRefunded;

@Entity
@Table(name = "Payment_table")
@Data
//<<< DDD / Aggregate Root
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long charge;

    private Long userId;

    private Long reservationId;

    private String name;

    private String impUid;

    private String status;

    @PostPersist
    public void onPostPersist() {
        PaymentCancelled paymentCancelled = new PaymentCancelled(this);
        paymentCancelled.publishAfterCommit();

        Paid paid = new Paid(this);
        paid.publishAfterCommit();

        PaymentFailed paymentFailed = new PaymentFailed(this);
        paymentFailed.publishAfterCommit();

        PaymentRefunded paymentRefunded = new PaymentRefunded(this);
        paymentRefunded.publishAfterCommit();

        PaymentRefundFailed paymentRefundFailed = new PaymentRefundFailed(this);
        paymentRefundFailed.publishAfterCommit();
    }

    public static PaymentRepository repository() {
        PaymentRepository paymentRepository = PaymentApplication.applicationContext.getBean(
            PaymentRepository.class
        );
        return paymentRepository;
    }

    //<<< Clean Arch / Port Method
    public static void receiveReservationInfo(
        FlightReservaionRequested flightReservaionRequested
    ) {
        //implement business logic here:

        /** Example 1:  new item 
        Payment payment = new Payment();
        repository().save(payment);

        */

        /** Example 2:  finding and process
        
        repository().findById(flightReservaionRequested.get???()).ifPresent(payment->{
            
            payment // do something
            repository().save(payment);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
