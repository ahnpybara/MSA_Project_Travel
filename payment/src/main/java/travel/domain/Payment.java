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

    private Long reservationId;

    private Long charge;

    private Long userId;

    private String name;

    private String imp_uid;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public static PaymentRepository repository() {
        PaymentRepository paymentRepository = PaymentApplication.applicationContext.getBean(PaymentRepository.class);
        return paymentRepository;
    }
}
