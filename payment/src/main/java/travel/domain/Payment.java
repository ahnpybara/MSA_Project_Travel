package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.PaymentApplication;
import travel.infra.RollBackException;

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

    // 예약 요청 이벤트를 수신받아서 결제 정보에 저장하는 메서드
    public static void reservationInfo(PaymentRequested paymentRequested) {

        try {
            Payment exsistPayment = repository().findByReservationId(paymentRequested.getId());
            
            // 결제 정보 중복 확인 -> 없다면 저장 -> 이미 있다면 상태만 변경
            if(exsistPayment == null) {
                // 결제 정보를 저장할 객체 생성
                Payment payment = new Payment();
                payment.setReservationId(paymentRequested.getId());
                payment.setName(paymentRequested.getName());
                payment.setCharge(paymentRequested.getCharge());
                payment.setUserId(paymentRequested.getUserId());
                payment.setStatus(PaymentStatus.결제전);
                repository().save(payment);
            } else {
                exsistPayment.setStatus(PaymentStatus.결제전);
            }

        } catch (Exception e) {
            // 여기서 예약 정보가 저장되지 않을 경우 결제 로직중 오류 발생 -> SAGA 패턴 시행 따라서 재시도 로직은 넣지 않음
            System.out.println("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e);
            throw new RollBackException("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e);
        }
    }
}
