package travel.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import travel.domain.PaymentRepository;
import travel.domain.PaymentStatus;
import travel.event.subscribe.FlightReservationCancelRequested;
import travel.event.subscribe.FlightReservationRequested;
import travel.exception.CustomException;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 예약 정보를 수신받아서 결제 정보로 저장하는 메서드
    @Transactional(rollbackFor = CustomException.class)
    public void createPayment(FlightReservationRequested flightReservationRequested) {
        try {
            travel.domain.Payment exsistPayment = paymentRepository.findByReservationId(flightReservationRequested.getId());
            
            if(exsistPayment == null) {
                travel.domain.Payment payment = new travel.domain.Payment();
                payment.setReservationId(flightReservationRequested.getId());
                payment.setName(flightReservationRequested.getName());
                payment.setCharge(flightReservationRequested.getCharge());
                payment.setUserId(flightReservationRequested.getUserId());
                payment.setStatus(PaymentStatus.결제전);
                paymentRepository.save(payment);
            } else {
                exsistPayment.setStatus(PaymentStatus.결제전);
            }

        } catch (Exception e) {
            // TODO : 예약 정보를 저장하는 도중 문제가 발생 -> 이를 예약 서비스로 알려야함 SAGA 패턴이 필요!! -> 현재는 재시도로 해결
            logger.error("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e);
            throw new CustomException("예약 정보를 저장하는 도중 예상치 못한 오류가 발생 : " + e, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }

    // 단순 변심으로 예약 취소시 결제 상태를 변경하는 메서드
    @Transactional(rollbackFor = CustomException.class)
    public void updatePayment(FlightReservationCancelRequested flightReservationCancelRequested) {
        try {

            travel.domain.Payment paymentInfo = paymentRepository.findByReservationId(flightReservationCancelRequested.getId());
            
            if(paymentInfo == null) {
                logger.error("해당 예약건에 대한 결제 정보가 존재하지 않습니다");
            } else {
                paymentInfo.setStatus(PaymentStatus.결제취소);
                paymentRepository.save(paymentInfo);
            }

        } catch (Exception e) {
            // TODO : 예약 정보를 수정하는 도중 문제가 발생 -> 이를 예약 서비스로 알려야함 SAGA 패턴이 필요!! -> 현재는 재시도로 해결
            logger.error("예약 정보를 수정하는 도중 예상치 못한 오류가 발생 : " + e);
            throw new CustomException("예약 정보를 수정하는 도중 예상치 못한 오류가 발생 : " + e, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }
}
