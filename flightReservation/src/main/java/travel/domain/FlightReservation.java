package travel.domain;



import javax.persistence.*;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import lombok.Data;
import travel.FlightReservationApplication;

@Entity
@Table(name = "FlightReservation_table")
@Data
//<<< DDD / Aggregate Root
public class FlightReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long flightId;

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

    private String reservationHash;


    private static final Logger log = LoggerFactory.getLogger(FlightReservation.class);
    
    @PostPersist
    public void onPostPersist() {
        FlightBookCompleted flightBookCompleted = new FlightBookCompleted(this);
        flightBookCompleted.publishAfterCommit();
    }

    public static FlightReservationRepository repository() {
        FlightReservationRepository flightReservationRepository = FlightReservationApplication.applicationContext.getBean(
            FlightReservationRepository.class
        );
        return flightReservationRepository;
    }


    //<<< Clean Arch / Port Method
    @Transactional
    public static void paymentComplete(Paid paid) {
        try {
            repository().findById(paid.getReservationId()).ifPresentOrElse(flightReservation->{
                flightReservation.setStatus(Status.예약완료);
                repository().save(flightReservation);

                FlightBookCompleted flightBookCompleted = new FlightBookCompleted(flightReservation);
                flightBookCompleted.publishAfterCommit();
    
            }, () -> {
                log.error("don't find flightReservation : ", paid.getReservationId());
    
            }); 
            } catch (Exception e) {
                log.error("paid event is failed" , e);
                throw e;
            }

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    @Transactional
    public static void paymentCancel(PaymentCancelled paymentCancelled) {
        try {
            repository().findById(paymentCancelled.getReservationId()).ifPresentOrElse(flightReservation->{
                flightReservation.setStatus(Status.결제취소);
                repository().save(flightReservation);

                FlightbookCancelled flightbookCancelled = new FlightbookCancelled(flightReservation);
                flightbookCancelled.publishAfterCommit();
    
            }, () -> {
                log.error("don't find flightReservation : ", paymentCancelled.getReservationId());
    
            }); 
            } catch (Exception e) {
                log.error("paymentCancelled event is failed" , e);
                throw e;
            }
    }
     
    
    
    @Transactional
    public static void paymentFailed(PaymentFailed paymentFailed){
        try {
        repository().findById(paymentFailed.getReservationId()).ifPresentOrElse(flightReservation->{
            flightReservation.setStatus(Status.결제실패);
            repository().save(flightReservation);
        }, () -> {
            log.error("don't find flightReservation : ", paymentFailed.getReservationId());

        }); 
        } catch (Exception e) {
            log.error("paymentFailed event is failed" , e);
            throw e;
        }
    }
    @Transactional
    public static void paymentCancelFailed(PaymentCancelFailed paymentCancelFailed){
        try {
            repository().findById(paymentCancelFailed.getReservationId()).ifPresentOrElse(flightReservation->{
                flightReservation.setStatus(Status.취소실패);
                repository().save(flightReservation);
            }, () -> {
                log.error("don't find flightReservation : ", paymentCancelFailed.getReservationId());
    
            }); 
            } catch (Exception e) {
                log.error("paymentCancelFailed event is failed" , e);
                throw e;
            }
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
