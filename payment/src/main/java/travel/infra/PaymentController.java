package travel.infra;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;
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

    // 결제전 결제 정보를 확인하는 메서드
    @PostMapping("/check")
    public ResponseEntity<String> checkPayment(@Valid @RequestBody PaymentDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터를 검증(null or empty)
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("merchant_uid cannot be null or empty");
        }

        // 서비스 레이어에서 결제 정보를 체크하는 메서드를 호출
        PaymentStatus paymentResult = paymentService.checkPayment(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Start Payment Failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 시작 실패");
        }

        System.out.println("Start Payment Success");
        return ResponseEntity.ok("사전 검증 진행 성공");
    }

    // 결제 사전 검증을 진행하는 메서드
    @PostMapping("/prepare")
    public ResponseEntity<String> preparePayment(@Valid @RequestBody PaymentDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터를 검증(null or empty)
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("merchant_uid cannot be null or empty");
        }

        // 서비스 레이어에서 사전 검증를 수행하는 메서드를 호출
        PaymentStatus paymentResult = paymentService.postPrepare(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Prepare payment failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사전 검증 실패");
        }

        System.out.println("Prepare payment Testing Processing...");
        return ResponseEntity.ok("사전 검증 진행 성공");
    }

    // 결제 사후 검증을 진행하는 메서드
    @PostMapping("/validate")
    public ResponseEntity<String> validatePayment(@Valid @RequestBody PaymentDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터를 검증(null or empty)
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("merchant_uid cannot be null or empty");
        }

        // 서비스 레이어에서 사후 검증를 수행하는 메서드를 호출
        PaymentStatus paymentResult = paymentService.validatePayment(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Validate payment failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사후 검증 실패");
        }

        System.out.println("Validate payment succeeded");
        return ResponseEntity.ok("사후 검증 성공");
    }

    // 결제 취소를 진행하는 메서드
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@Valid @RequestBody PaymentDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터를 검증
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("merchant_uid cannot be null or empty");
        }
             
        // 서비스 레이어에서 결제 취소를 수행하는 메서드를 호출
        PaymentStatus paymentResult = paymentService.cancelPayment(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Payment cancellation failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 실패");
        }

        System.out.println("Payment cancellation succeeded");
        return ResponseEntity.ok("결제 취소 성공");
    }

    // 결제 실패 요청을 받는 메서드
    @PostMapping("/fail")
    public ResponseEntity<String> failPayment(@Valid @RequestBody PaymentDTO request, BindingResult bindingResult) {

        // 클라이언트에서 전달된 파라미터를 검증
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("merchant_uid cannot be null or empty");
        }

        // 서비스 레이어에서 결제 실패를 알리는 메서드를 호출
        PaymentStatus paymentResult = paymentService.paymentFailNotify(request);

        if (paymentResult == PaymentStatus.실패) {
            System.out.println("Payment cancellation Notify failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 알림 실패");
        }

        System.out.println("Payment cancellation Notify succeeded");
        return ResponseEntity.ok("결제 취소 알림 성공");
    }
}