package travel.infra;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import travel.domain.Paid;
import travel.domain.PaymentCancelFailed;
import travel.domain.PaymentCancelled;
import travel.domain.PaymentDTO;
import travel.domain.PaymentFailed;
import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.math.BigDecimal;

import javax.annotation.PostConstruct;

@Service
public class PaymentService {

    private IamportClient api;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        this.api = new IamportClient(apiKey, apiSecret);
    }

    public travel.domain.Payment postPrepare(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        try {
            PrepareData prepareData = new PrepareData(request.getMerchant_uid(), request.getAmount());
            api.postPrepare(prepareData); // 사전 등록 API
            travel.domain.Payment preValidateSucceed = new travel.domain.Payment();
            preValidateSucceed.setStatus(PaymentStatus.결제중);
            return preValidateSucceed;
        } catch (IamportResponseException | IOException e) {
            return handlePaymentFailed(reservationId, "Prepare payment failed: " + e.getMessage(), PaymentStatus.결제실패);
        }
    }

    @Transactional
    public travel.domain.Payment validatePayment(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        try {
            postPayment = paymentRepository
                    .findByReservationId(Long.valueOf(reservationId))
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

            BigDecimal postAmount = BigDecimal.valueOf(postPayment.getCharge());

            IamportResponse<Payment> iamportResponse = api.paymentByImpUid(request.getImp_uid());
            BigDecimal paidAmount = iamportResponse.getResponse().getAmount();

            if (!postAmount.equals(paidAmount)) {
                CancelData cancelData = new CancelData(iamportResponse.getResponse().getImpUid(), true);
                api.cancelPaymentByImpUid(cancelData);
                throw new IllegalStateException("Payment validation failed");
            }

            postPayment.setImp_uid(iamportResponse.getResponse().getImpUid());
            postPayment.setStatus(PaymentStatus.결제완료); 
            paymentRepository.save(postPayment);

            Paid paid = new Paid(postPayment);
            paid.publishAfterCommit();

            return postPayment;

        } catch (IamportResponseException | IOException | IllegalStateException e) {
            return handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제실패);
        }
    }

    @Transactional
    public travel.domain.Payment cancelPayment(String merchant_uid) {
        String reservationId = getReservationNumber(merchant_uid);
        travel.domain.Payment postPayment = null;
        try {
            postPayment = paymentRepository.findByReservationId(Long.valueOf(reservationId))
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
            IamportResponse<Payment> iamportResponse = api.paymentByImpUid(postPayment.getImp_uid());
            Payment payment = iamportResponse.getResponse();

            if (payment.getStatus().equals("paid")) {
                CancelData cancelData = new CancelData(payment.getImpUid(), true);
                api.cancelPaymentByImpUid(cancelData);

                postPayment.setStatus(PaymentStatus.결제취소);
                paymentRepository.save(postPayment);

                PaymentCancelled paymentCancelled = new PaymentCancelled(postPayment);
                paymentCancelled.publishAfterCommit();

                return postPayment;
            } else {
                throw new IllegalStateException("Payment is not completed yet");
            }
        } catch (IamportResponseException | IOException e) {
            return handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제취소실패);
        }
    }

    public String getReservationNumber(String merchant_uid) {
        String[] parts = merchant_uid.split("_");
        if (parts.length > 0) {
            return parts[0]; // "_" 앞의 숫자 부분만 반환
        }
        return ""; // "_"가 없는 경우 빈 문자열 반환
    }

    private travel.domain.Payment handlePaymentFailed(String reservationId, String errorMessage, PaymentStatus status) {
        AbstractEvent event;
        switch (status) {
            case 결제실패:
                PaymentFailed paymentFailed = new PaymentFailed();
                paymentFailed.setReservationId(Long.valueOf(reservationId));
                event = paymentFailed;
                break;
            case 결제취소실패:
                PaymentCancelFailed paymentCancelFailed = new PaymentCancelFailed();
                paymentCancelFailed.setReservationId(Long.valueOf(reservationId));
                event = paymentCancelFailed;
                break;
            default:
                throw new IllegalArgumentException("Invalid payment status: " + status);
        }

        event.publishAfterCommit();
        System.out.println(errorMessage);
        travel.domain.Payment failedPayment = new travel.domain.Payment();
        failedPayment.setStatus(status);
        return failedPayment;
    }

}