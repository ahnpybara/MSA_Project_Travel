package travel.infra;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import travel.domain.PaymentRepository;
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
        try {
            paymentService.postPrepare(request);
            System.out.println("Prepare payment succeeded");
            return ResponseEntity.ok("사전 검증 성공");
        } catch (Exception e) {
            System.out.println("Prepare payment failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사전 검증 실패: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validatePayment(@RequestBody PaymentDTO request) {
        try {
            paymentService.validatePayment(request);
            System.out.println("Validate payment succeeded");
            return ResponseEntity.ok("사후 검증 성공");
        } catch (Exception e) {
            System.out.println("Validate payment failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사후 검증 실패: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentDTO request) {
        try {
            paymentService.cancelPayment(request.getMerchant_uid());
            System.out.println("Payment cancellation succeeded");
            return ResponseEntity.ok("결제 취소 성공");
        } catch (Exception e) {
            System.out.println("Payment cancellation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 취소 실패: " + e.getMessage());
        }
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

            return ResponseEntity.ok("결제 취소됨 알림 성공");

        } catch (Exception e) {
            System.out.println("Error occurred while processing payment cancellation: " + e.getMessage());
            return new ResponseEntity<>("Error occurred while processing payment cancellation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}