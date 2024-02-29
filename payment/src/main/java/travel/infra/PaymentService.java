package travel.infra;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import travel.domain.FlightCancelRequested;
import travel.domain.Paid;
import travel.domain.PaymentCancelFailed;
import travel.domain.PaymentCancelled;
import travel.domain.PaymentDTO;
import travel.domain.PaymentFailed;
import travel.domain.PaymentRepository;
import travel.domain.PaymentRequested;
import travel.domain.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import javax.annotation.PostConstruct;

@Service
public class PaymentService {

    private IamportClient api;

    @Autowired
    private PaymentRepository paymentRepository;

    // 포트원 api 요청에 이용되는 키 정보
    @Value("${iamport.api.key}")
    private String apiKey; 

    @Value("${iamport.api.secret}")
    private String apiSecret;

    // 포트원서버와 통신할 IAMPort 클라이언트를 생성
    @PostConstruct
    public void init() {
        this.api = new IamportClient(apiKey, apiSecret);
    }

    // 결제를 진행하기전 DB에 결제정보가 있는지 확인하는 메서드(이거 없으면 포트원 방식으로 진행이 안됨)
    public PaymentStatus checkPayment(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        try {
            postPayment = paymentRepository.findByReservationId(Long.valueOf(reservationId));
            if (postPayment != null) return PaymentStatus.성공;
            else throw new IllegalAccessException("결제 정보가 존재하지 않습니다");
        } catch (Exception e) {
            return handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제실패);
        }
    }

    // 결제 사전 검증 api를 호출하는 메서드
    public PaymentStatus postPrepare(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        try {
            // 사전 검증 API 요청을 하기 위한 데이터를 생성후 사전 검증 api를 요청합니다
            PrepareData prepareData = new PrepareData(request.getMerchant_uid(), request.getAmount());
            api.postPrepare(prepareData); 
            return PaymentStatus.성공;
        } catch (Exception e) {
            return handlePaymentFailed(reservationId, "결제 사전검증 도중 문제가 발생하였습니다", PaymentStatus.결제실패);
        }
    }

    // 결제 사후 검증 api를 호출하는 메서드
    @Transactional(rollbackFor = RollBackException.class)
    public PaymentStatus validatePayment(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        try {
            // 결제된 예약 id를 통해 DB에서 결제 정보를 찾습니다
            postPayment = paymentRepository.findByReservationId(Long.valueOf(reservationId));
            BigDecimal postAmount = BigDecimal.valueOf(postPayment.getCharge()); // 결제 정보중 금액을 가져옵니다

            // 포트원에서 결제된 결제정보와 금액을 가져옵니다
            IamportResponse<Payment> iamportResponse = api.paymentByImpUid(request.getImp_uid());
            BigDecimal paidAmount = iamportResponse.getResponse().getAmount(); // 결제 정보중 금액을 가져옵니다
            postPayment.setImp_uid(iamportResponse.getResponse().getImpUid()); // 포트원에서 관리하는 결제 id를 할당합니다

            // DB에 저장된 결제 정보의 금액과 포트원에 저장된 결제 정보의 금액을 비교해서 같으면 검증 성공 다르면 실패 처리가 됩니다
            if (!postAmount.equals(paidAmount)) {
                throw new IllegalStateException("실제로 결제된 금액과 주문 정보의 결제 금액이 다릅니다");
            }

            // 결제 완료시 상태를 변경해서 저장합니다
            postPayment.setStatus(PaymentStatus.결제완료);
            paymentRepository.save(postPayment);
            
            // 결제됨 이벤트를 발행합니다
            Paid paid = new Paid(postPayment);
            paid.publishAfterCommit();
            
            return PaymentStatus.성공;

        } catch (Exception e) {
            handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제취소실패);
            throw new RollBackException("사후 검증이 실패되었습니다 : " + e);
        }
    }

    // 결제 취소 api를 호출하는 메서드
    @Transactional(rollbackFor = RollBackException.class)
    public PaymentStatus cancelPayment(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());
        travel.domain.Payment postPayment = null;
        String imp_uid = request.getImp_uid();
        IamportResponse<Payment> iamportResponse = null;

        try {
            // 결제된 예약 id를 통해 DB에서 결제 정보에서 결제번호를 찾아서, 포트원에서 결제된 결제 정보를 검색합니다  
            postPayment = paymentRepository.findByReservationId(Long.valueOf(reservationId));

            // 사후 검증이 실패했을 때 실행되는 결제 취소 요청인지, 별개의 환불요청인지 구분하기 위함
            if(imp_uid == null) {
                System.out.println("환불요청");
                iamportResponse = api.paymentByImpUid(postPayment.getImp_uid());
            } else {
                System.out.println("사후 검증이 실패");
                iamportResponse = api.paymentByImpUid(imp_uid);            
            }

            Payment payment = iamportResponse.getResponse();

            // 포트원에서 처리된 결제가 결제완료됨 상태이면 결제취소 api를 호출합니다
            if (payment.getStatus().equals("paid")) {
                CancelData cancelData = new CancelData(payment.getImpUid(), true);
                api.cancelPaymentByImpUid(cancelData);

                // 결제 실패시 상태를 변경해서 저장합니다
                postPayment.setStatus(PaymentStatus.결제취소);
                paymentRepository.save(postPayment);

                // 결제 실패됨 이벤트를 발행합니다
                PaymentCancelled paymentCancelled = new PaymentCancelled(postPayment);
                paymentCancelled.publishAfterCommit();
                
                return PaymentStatus.성공;

            } else {
                throw new IllegalStateException("Payment is not completed yet");
            }
        } catch (Exception e) {
            handlePaymentFailed(reservationId, e.getMessage(), PaymentStatus.결제취소실패);
            throw new RollBackException("결제 취소가 실패되었습니다 : " + e);
        }
    }

    // 사용자가 결제 도중 취소하거나 결제 url가 만료 되었을 때 이를 알리기 위한 메서드(SAGA패턴)
    public PaymentStatus paymentFailNotify(PaymentDTO request) {
        String reservationId = getReservationNumber(request.getMerchant_uid());

        try {
            // 결제에 실패했다는걸 알리기 위해 결제 실패 이벤트를 발행
            PaymentCancelled paymentCancelled = new PaymentCancelled();
            paymentCancelled.setReservationId(Long.valueOf(reservationId));
            paymentCancelled.publishAfterCommit();
            System.out.println("Payment cancellation notify succeeded");
            return PaymentStatus.성공;

        } catch (Exception e) {
            // 사용자가 결제 취소 및 실패시 결제 실패를 알려야 하지만, 이때 알리는 도중에 문제 발생시 catch문에서 한번더 알리기
            return handlePaymentFailed(reservationId, "결제 취소를 알리는 도중 문제가 발생했습니다", PaymentStatus.결제실패);
        }
    }

    // merchant_uid에서 예약 번호를 추출하는 메서드
    public String getReservationNumber(String merchant_uid) {
        String[] parts = merchant_uid.split("_");
        if (parts.length > 0) {
            return parts[0]; // "_" 앞의 숫자 부분만 반환
        }
        return ""; // "_"가 없는 경우 빈 문자열 반환
    }

    // 각 단계에서 예외 발생시 이를 처리하고 실패 이벤트(보상 트랜잭션)를 발행하는 메서드(SAGA패턴)
    private PaymentStatus handlePaymentFailed(String reservationId, String errorMessage, PaymentStatus status) {
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
        event.publishAfterCommit(); // TODO 재시도 로직 추가
        System.out.println(errorMessage);
        return PaymentStatus.실패;
    }

    // 예약 정보를 토대로 결제 정보를 저장하는 메서드
    @Transactional(rollbackFor = Exception.class)
    public void createPayment(PaymentRequested paymentRequested) {
        try {
            travel.domain.Payment exsistPayment = paymentRepository.findByReservationId(paymentRequested.getId());
            
            // 결제 정보 중복 확인 -> 없다면 저장 -> 이미 있다면 상태만 변경
            if(exsistPayment == null) {
                // 결제 정보를 저장할 객체 생성
                travel.domain.Payment payment = new travel.domain.Payment();
                payment.setReservationId(paymentRequested.getId());
                payment.setName(paymentRequested.getName());
                payment.setCharge(paymentRequested.getCharge());
                payment.setUserId(paymentRequested.getUserId());
                payment.setStatus(PaymentStatus.결제전);
                paymentRepository.save(payment);
            } else {
                exsistPayment.setStatus(PaymentStatus.결제전);
            }

        } catch (Exception e) {
            throw new RollBackException("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e);
        }
    }

    // 예약 취소시 결제 상태를 예약취소로 변경하는 메서드
    @Transactional(rollbackFor = Exception.class)
    public void updatePayment(FlightCancelRequested flightCancelRequested) {
        try {
            travel.domain.Payment paymentInfo = paymentRepository.findByReservationId(flightCancelRequested.getId());
            
            // 결제 정보 중복 확인 -> 없다면 저장 -> 이미 있다면 상태만 변경
            if(paymentInfo == null) {
                System.out.println("해당 예약건에 대한 결제 정보가 존재하지 않습니다");
            } else {
                paymentInfo.setStatus(PaymentStatus.결제취소);
                paymentRepository.save(paymentInfo);
            }

        } catch (Exception e) {
            throw new RollBackException("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e);
        }
    }
}