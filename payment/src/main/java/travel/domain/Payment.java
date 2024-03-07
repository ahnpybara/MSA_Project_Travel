package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.PaymentApplication;

@Entity
@Table(name = "Payment_table")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long charge;

    private Long userId;

    private Long reservationId;

    private String name;

    private String impUid;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.결제전;

    public static PaymentRepository repository() {
        PaymentRepository paymentRepository = PaymentApplication.applicationContext.getBean(
            PaymentRepository.class);
        return paymentRepository;
    }
}
