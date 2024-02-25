package travel.domain;


import lombok.*;
import travel.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class PaymentCnlRequested extends AbstractEvent {

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
    private String reservationHash;
    private String name;

    public PaymentCnlRequested(FlightReservation aggregate) {
        super(aggregate);
    }

    public PaymentCnlRequested() {
        super();
    }
}
//>>> DDD / Domain Event
