package travel.infra;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;
import travel.dto.AfterPaymentDTO;
import travel.dto.CheckPaymentDTO;
import travel.dto.FailPaymentDTO;
import travel.dto.PreparePaymentDTO;
import travel.dto.RefundPaymentDTO;
import travel.event.publish.AbstractEvent;
import travel.event.publish.Paid;
import travel.event.publish.PaymentCancelled;
import travel.event.publish.PaymentFailed;
import travel.event.publish.PaymentRefundFailed;
import travel.event.publish.PaymentRefunded;
import travel.exception.CustomException;
import travel.exception.RetryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import javax.annotation.PostConstruct;

@Service
public class PaymentAPIService {

    private IamportClient api;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 포트원서버와 통신할 IAMPort 클라이언트를 생성
    @PostConstruct
    public void init() {
        this.api = new IamportClient(apiKey, apiSecret);
    }

    // 결제를 진행하기전 DB에 결제되어야할 정보가 있는지 확인하는 메서드
    public PaymentStatus checkPaymentInfo(CheckPaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        try {
            postPayment = paymentRepository.findByReservationIdAndCategory(Long.valueOf(reservationId), request.getCategory());
            if (postPayment != null && postPayment.getStatus() != PaymentStatus.결제완료)
                return PaymentStatus.성공;
            else
                throw new IllegalAccessException("결제 정보가 존재하지 않습니다");
        } catch (IllegalAccessException e) {
            return handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제실패, request.getCategory());
        } catch (Exception e) {
            return handlePaymentFailed(reservationId, "알 수 없는 오류가 발생했습니다 : " + e, PaymentStatus.결제실패, request.getCategory());
        }
    }

    // 결제가 실제로 진행되기전 포트원의 사전 검증 api를 호출하는 메서드
    public PaymentStatus preparePayment(PreparePaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        try {
            PrepareData prepareData = new PrepareData(request.getMerchant_uid(), request.getAmount());
            api.postPrepare(prepareData);
            return PaymentStatus.성공;
        } catch (Exception e) {
            return handlePaymentFailed(reservationId, "결제 사전검증 도중 문제가 발생하였습니다 : " + e, PaymentStatus.결제실패, request.getCategory());
        }
    }

    // 결제가 처리된 후 사후 검증 api를 호출하는 메서드(결제 사후 검증은 내부 로직이기 때문에 카테고리가 필요없습니다!!)
    @Transactional(rollbackFor = CustomException.class)
    public PaymentStatus validatePayment(AfterPaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        try {

            // 결제된 예약 id로 결제 테이블에서 결제 정보를 찾습니다
            postPayment = paymentRepository.findByReservationIdAndCategory(Long.valueOf(reservationId), request.getCategory());
            BigDecimal postAmount = BigDecimal.valueOf(postPayment.getCharge());

            // 포트원에서 결제처리된 결제정보를 가져옵니다
            IamportResponse<Payment> iamportResponse = api.paymentByImpUid(request.getImp_uid());
            BigDecimal paidAmount = iamportResponse.getResponse().getAmount();
            postPayment.setImpUid(iamportResponse.getResponse().getImpUid());

            // DB에 저장된 결제 정보의 금액과 포트원에 저장된 결제 정보의 금액을 비교해서 검증합니다
            if (!postAmount.equals(paidAmount)) {
                throw new IllegalStateException("실제로 결제된 금액과 주문 정보의 결제 금액이 다릅니다");
            }

            postPayment.setStatus(PaymentStatus.결제완료);
            travel.domain.Payment paymentStatus = paymentRepository.save(postPayment);

            if (paymentStatus.getStatus() == PaymentStatus.결제완료) {
                Paid paid = new Paid(postPayment);
                paid.publishAfterCommit();
                return PaymentStatus.성공;
            } else {
                throw new RuntimeException("결제완료 상태로 제대로 변경이 되지 않았습니다.");
            }

        } catch (IllegalStateException e) {
            return handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제실패, request.getCategory());
        } catch (RuntimeException e) {
            handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제실패, request.getCategory());
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        } catch (Exception e) {
            handlePaymentFailed(reservationId, "결제 사후검증 도중 알 수 없는 문제가 발생하였습니다 : " + e, PaymentStatus.결제실패, request.getCategory());
            throw new CustomException("결제 사후검증 도중 알 수 없는 문제가 발생하였습니다 : " + e, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }

    // 환불처리를 위한 api를 호출하는 메서드
    @Transactional(rollbackFor = CustomException.class)
    public PaymentStatus refundPayment(RefundPaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        IamportResponse<Payment> iamportResponse = null;

        try {
            postPayment = paymentRepository.findByReservationIdAndCategory(Long.valueOf(reservationId), request.getCategory());

            // 사후 검증이 실패했을 때 실행되는 결제 취소 요청인지, 별개의 환불요청인지 구분하기 위함
            if (request.getImp_uid() == null) {
                logger.info("\n환불요청\n");
                iamportResponse = api.paymentByImpUid(postPayment.getImpUid());
                postPayment.setStatus(PaymentStatus.환불완료);
            } else {
                logger.info("\n사후 검증로 실패로 인한 환불\n");
                iamportResponse = api.paymentByImpUid(request.getImp_uid());
                postPayment.setStatus(PaymentStatus.결제취소);
            }

            travel.domain.Payment paymentStatus = paymentRepository.save(postPayment);
            Payment payment = iamportResponse.getResponse();
            Boolean paymentState = payment.getStatus().equals("paid") && ((paymentStatus.getStatus() == PaymentStatus.환불완료) || (paymentStatus.getStatus() == PaymentStatus.결제취소)); 
            // 포트원에서 처리된 결제가 결제완료됨 상태이면 환불처리 api를 호출합니다
            if (paymentState) {
                CancelData cancelData = new CancelData(payment.getImpUid(), true);
                api.cancelPaymentByImpUid(cancelData);
                PaymentRefunded paymentRefunded = new PaymentRefunded(postPayment);
                paymentRefunded.publishAfterCommit();
                return PaymentStatus.성공;
            } else {
                throw new IllegalStateException("환불처리할 결제건이 아직 결제완료된 상태가 아니거나, 환불처리 상태 반영이 되지 않았습니다");
            }
        } catch (IllegalStateException e) {
            handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.환불실패, request.getCategory());
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        } catch (Exception e) {
            return handlePaymentFailed(reservationId, "알 수 없는 이유로 환불처리에 실패했습니다. " + e, PaymentStatus.환불실패, request.getCategory());
        }
    }

    // 사용자가 결제 도중 취소하거나 결제 url이 만료된 상황을 처리할 메서드
    @Retryable(value = RetryException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public PaymentStatus cancelPayment(FailPaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;

        try {
            postPayment = paymentRepository.findByReservationIdAndCategory(Long.valueOf(reservationId), request.getCategory());
            postPayment.setStatus(PaymentStatus.결제취소);
            travel.domain.Payment paymentStatus = paymentRepository.save(postPayment);

            // 결제에 실패했다는걸 알리기 위해 결제 실패 이벤트를 발행합니다(SAGA)
            if (paymentStatus.getStatus() == PaymentStatus.결제취소) {
                PaymentCancelled paymentCancelled = new PaymentCancelled(postPayment);
                paymentCancelled.publishAfterCommit();
                return PaymentStatus.성공;
            } else {
                throw new IllegalStateException("결제실패 처리가 제대로 이루워지지 않았습니다.");
            }
        } catch (IllegalStateException e) {
            // TODO 결제 실패처리를 실패했을 경우? 이는 SAGA로 해결할 수 없음 고로 재시도로 해결??
            logger.error(e.getMessage());
            throw new RetryException(e.getMessage());
        } catch (Exception e) {
            logger.error("\n알 수 없는 이유로 결제취소 처리에 문제가 발생했습니다 : " + e);
            throw new RetryException("결제취소 처리중 오류가 발생했습니다. 재시도를 수행합니다.");
        }
    }

    // 재시도를 했음에도 실패했을 경우 해당 메서드가 호출됩니다.
    @Recover
    public PaymentStatus handleFailPaymentException(Exception e) {
        logger.error("\n재시도를 했음에도 결제취소 처리에 실패했습니다. 이는 관리자가 직접 처리를 해야합니다.\n", e);
        return PaymentStatus.실패;
    }

    // merchant_uid에서 예약 번호를 추출하는 메서드
    public String getReservationNumber(String merchant_uid) {
        String[] parts = merchant_uid.split("_");
        if (parts.length > 0) {
            return parts[0]; // "_" 앞의 숫자 부분만 반환
        }
        return ""; // "_"가 없는 경우 빈 문자열 반환
    }

    // 각 결제 단계에서 예외 발생시 이를 처리하고 실패 이벤트(보상 트랜잭션)를 발행하는 메서드(SAGA패턴)
    private PaymentStatus handlePaymentFailed(String reservationId, String errorMessage, PaymentStatus status, String category) {
        AbstractEvent event;
        switch (status) {
            case 결제실패:
                PaymentFailed paymentFailed = new PaymentFailed();
                paymentFailed.setReservationId(Long.valueOf(reservationId));
                paymentFailed.setCategory(category);
                event = paymentFailed;
                break;
            case 환불실패:
                PaymentRefundFailed paymentRefundFailed = new PaymentRefundFailed();
                paymentRefundFailed.setReservationId(Long.valueOf(reservationId));
                paymentRefundFailed.setCategory(category);
                event = paymentRefundFailed;
                break;
            default:
                throw new IllegalArgumentException("Invalid payment status: " + status);
        }
        event.publishAfterCommit();
        logger.info("\n"+ errorMessage + "\n");
        return PaymentStatus.실패;
    }
}