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
    public ResponseEntity<travel.domain.Payment> preparePayment(@RequestBody PaymentDTO request) {
        travel.domain.Payment payment = paymentService.postPrepare(request);
        System.out.println("Prepare payment succeeded");
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/validate")
    public ResponseEntity<travel.domain.Payment> validatePayment(@RequestBody PaymentDTO request) {
        travel.domain.Payment payment = paymentService.validatePayment(request);
        System.out.println("Validate payment succeeded");
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/cancel")
    public ResponseEntity<travel.domain.Payment> cancelPayment(@RequestBody PaymentDTO request) {
        travel.domain.Payment payment = paymentService.cancelPayment(request.getMerchant_uid());
        System.out.println("Payment cancellation succeeded");
        return ResponseEntity.ok(payment);
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
            return new ResponseEntity<>("Error occurred while processing payment cancellation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}