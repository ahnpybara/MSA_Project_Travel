package travel.infra;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;
import travel.domain.PaymentCancelled;
import travel.domain.PaymentDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/payments")
@Transactional
public class PaymentController {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<String> preparePayment(@RequestBody PaymentDTO request) {
        PaymentStatus paymentResult = paymentService.postPrepare(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Prepare payment failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사전 검증 실패");
        }

        System.out.println("Prepare payment Testing Processing...");
        return ResponseEntity.ok("사전 검증 진행 성공");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validatePayment(@RequestBody PaymentDTO request) {
        PaymentStatus paymentResult = paymentService.validatePayment(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Validate payment failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사후 검증 실패");
        }

        System.out.println("Validate payment succeeded");
        return ResponseEntity.ok("사후 검증 성공");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentDTO request) {
        PaymentStatus paymentResult = paymentService.cancelPayment(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Payment cancellation failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 실패");
        }

        System.out.println("Payment cancellation succeeded");
        return ResponseEntity.ok("결제 취소 성공");
    }

    @PostMapping("/fail")
    public ResponseEntity<String> failPayment(@RequestBody PaymentDTO request) {
        try {
            String reservationId = paymentService.getReservationNumber(request.getMerchant_uid());
            travel.domain.Payment payment = paymentRepository
                    .findByReservationId(Long.valueOf(reservationId))
                    .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

            PaymentCancelled paymentCancelled = new PaymentCancelled(payment);
            paymentCancelled.publishAfterCommit();

            System.out.println("Payment cancellation notify succeeded");

            return ResponseEntity.ok("Payment cancellation notify succeeded");

        } catch (Exception e) {
            System.out.println("Error occurred while processing payment cancellation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 요청 알림 실패");
        }
    }
}