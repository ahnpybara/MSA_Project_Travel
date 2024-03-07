package travel.infra;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import travel.domain.*;
import travel.events.publish.FlightReservationCancelled;
import travel.events.publish.FlightReservationFailed;
import travel.events.publish.FlightReservationRefunded;
import travel.events.subscribe.Paid;
import travel.events.subscribe.PaymentCancelled;
import travel.events.subscribe.PaymentFailed;
import travel.events.subscribe.PaymentRefunded;
import travel.events.subscribe.PaymentRefundFailed;
import travel.exception.CustomException;

@Service
public class FlightReservationEventService {

    @Autowired
    private FlightReservationRepository flightReservationRepository;



    private static final Logger logger = LoggerFactory.getLogger("Logger");

    // paid 이벤트 수신 처리
    @Transactional
    public void paymentComplete(Paid paid) {
        try {
            FlightReservation flightReservation = flightReservationRepository
                    .findById(paid.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("항공 예약정보가 존재하지 않습니다."));
            flightReservation.setStatus(Status.예약완료);
            flightReservationRepository.save(flightReservation);
            logger.info("\nFlightReservaionId: " + paid.getReservationId() + " 결제 완료되어 예약이 확정되었습니다. ");
        } catch (IllegalArgumentException e) {
            logger.error("\n message", e);
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 항공예약 상태를 변경하지 못하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }

    }

    // paymentRefund 이벤트 수신 처리 + flightReservaionRefunded 이벤트 발행.
    @Transactional
    public void paymentRefund(PaymentRefunded paymentRefunded) {
        try {
            FlightReservation flightReservation = flightReservationRepository
                    .findById(paymentRefunded.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("항공 예약정보가 존재하지 않습니다."));
            flightReservation.setStatus(Status.환불완료);
            flightReservationRepository.save(flightReservation);
            logger.info("\nFlightReservaionId: " + paymentRefunded.getReservationId() + " 환불 완료되어 예약이 취소되었습니다.. ");

       
            FlightReservationRefunded flightReservationRefunded = new FlightReservationRefunded(flightReservation);
            flightReservationRefunded.publishAfterCommit();
        } catch (IllegalArgumentException e) {
            logger.error("\n message", e);
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 항공예약 상태를 변경하지 못하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }

    }

    // paymentRefundFailed 이벤트 수신 처리
    @Transactional
    public void paymentRefundFail(PaymentRefundFailed paymentRefundFailed) {
        try {
            FlightReservation flightReservation = flightReservationRepository
                    .findById(paymentRefundFailed.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("항공 예약정보가 존재하지 않습니다."));
            flightReservation.setStatus(Status.환불실패);
            flightReservationRepository.save(flightReservation);
            logger.info("\nFlightReservaionId: " + paymentRefundFailed.getReservationId() + " 환불이 실패 했습니다.");

        } catch (IllegalArgumentException e) {
            logger.error("\n message", e);
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 항공예약 상태를 변경하지 못하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }

    }

    // paymentCancel 이벤트 수신 처리
    @Transactional
    public void paymentCancel(PaymentCancelled paymentCancelled) {
        try {
            FlightReservation flightReservation = flightReservationRepository
                    .findById(paymentCancelled.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("항공 예약정보가 존재하지 않습니다."));
            flightReservation.setStatus(Status.결제취소);
            flightReservationRepository.save(flightReservation);
            logger.info("\nFlightReservaionId: " + paymentCancelled.getReservationId() + " 결제를 취소 했습니다.");

            FlightReservationCancelled flightReservationCancelled = new FlightReservationCancelled(flightReservation);
            flightReservationCancelled.publishAfterCommit();

        } catch (IllegalArgumentException e) {
            logger.error("\n message", e);
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 항공예약 상태를 변경하지 못하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }

    }

    // paymentFail 이벤트 수신 처리
    @Transactional
    public void paymentFail(PaymentFailed paymentFailed) {
        try {
            FlightReservation flightReservation = flightReservationRepository
                    .findById(paymentFailed.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("항공 예약정보가 존재하지 않습니다."));
            flightReservation.setStatus(Status.결제실패);
            flightReservationRepository.save(flightReservation);
            logger.info("\nFlightReservaionId: " + paymentFailed.getReservationId() + " 결제가 실패 했습니다.");

            FlightReservationFailed flightReservationFailed = new FlightReservationFailed(flightReservation);
            flightReservationFailed.publishAfterCommit();

        } catch (IllegalArgumentException e) {
            logger.error("\n message", e);
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 항공예약 상태를 변경하지 못하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }

    }

}
