package travel.infra;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;
import travel.dto.AfterPaymentDTO;
import travel.dto.CheckPaymentDTO;
import travel.dto.FailPaymentDTO;
import travel.dto.PreparePaymentDTO;
import travel.dto.RefundPaymentDTO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/payments")
@Transactional
public class PaymentController {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentAPIService paymentService;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 결제전 결제 정보를 확인하는 메서드
    @PostMapping("/check")
    public ResponseEntity<String> checkPaymentInfo(@Valid @RequestBody CheckPaymentDTO request, BindingResult bindingResult) {

        // TODO 여기도 SAGA 패턴이 추가 되어야 함!! -> 하지만 이가 없으면 진행이 안되고 없을리가 없음!!
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("누락된 인자가 존재합니다. 필요인자 : 예약번호, 카테고리");
        }    

        PaymentStatus paymentResult = paymentService.checkPaymentInfo(request);

        if (paymentResult == PaymentStatus.실패) {
            logger.info("\nCheck PaymentInfo Failed\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제될 정보가 존재하지 않습니다. 예약이 제대로 되었는지 확인해주세요");
        }

        logger.info("\nCheck PaymentInfo Succeed\n");
        return ResponseEntity.ok("결제 정보 확인에 성공했습니다. 이제부터 진행됩니다");
    }

    // 결제 사전 검증을 진행하는 메서드
    @PostMapping("/prepare")
    public ResponseEntity<String> preparePayment(@Valid @RequestBody PreparePaymentDTO request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("누락된 인자가 존재합니다. 필요인자 : 예약번호, 카테고리, 가격");
        }    

        PaymentStatus paymentResult = paymentService.preparePayment(request);

        if (paymentResult == PaymentStatus.실패) {
            logger.info("\nPrepare Payment Failed\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제될 정보가 일치하지 않습니다. 가격이 올바른지 확인하세요");
        }

        logger.info("\nPrepare Payment Processing\n");
        return ResponseEntity.ok("결제 사전 검증에 성공했습니다. 이제 실제 결제가 진행됩니다");
    }

    // 결제 사후 검증을 진행하는 메서드
    @PostMapping("/validate")
    public ResponseEntity<String> validatePayment(@Valid @RequestBody AfterPaymentDTO request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("누락된 인자가 존재합니다. 필요인자 : 예약번호, 결제번호");
        }   

        PaymentStatus paymentResult = paymentService.validatePayment(request);

        if (paymentResult == PaymentStatus.실패) {
            logger.info("\nValidate Payment Failed\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("실제 결제되어야할 정보와 결제된 정보가 일치하지 않습니다. 환불이 진행됩니다");
        }

        logger.info("\nValidate Payment Succeeded\n");
        return ResponseEntity.ok("결제 사후 검증에 성공했습니다. 결제가 완료됩니다.");
    }

    // 환불 요청을 처리하는 메서드
    @PostMapping("/refund")
    public ResponseEntity<String> refundPayment(@Valid @RequestBody RefundPaymentDTO request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("누락된 인자가 존재합니다. 필요인자 : 예약번호, 카테고리");
        }   

        PaymentStatus paymentResult = paymentService.refundPayment(request);

        if (paymentResult == PaymentStatus.실패) {
            logger.info("\nRefund Payment Failed\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("환불처리에 실패했습니다. 결제정보를 다시 확인해주세요");
        }

        logger.info("\nRefund Payment Succeeded\n");
        return ResponseEntity.ok("환불처리에 성공했습니다.");
    }

    // 결제취소 요청을 받아서 처리하는 메서드
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(@Valid @RequestBody FailPaymentDTO request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {logger.error(error.getDefaultMessage());});
            return ResponseEntity.badRequest().body("누락된 인자가 존재합니다. 필요인자 : 예약번호, 카테고리");
        }   

        PaymentStatus paymentResult = paymentService.cancelPayment(request);

        if (paymentResult == PaymentStatus.실패) {
            logger.info("\nPayment Cancellation Failed\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 취소 처리에 실패했습니다. 관리자에게 문의 해주세요");
        }

        logger.info("\nPayment Cancellation Succeeded\n");
        return ResponseEntity.ok("결제 취소 처리에 성공했습니다.");
    }
}