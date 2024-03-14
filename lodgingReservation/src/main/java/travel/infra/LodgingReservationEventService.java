package travel.infra;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import travel.domain.LodgingReservation;
import travel.domain.LodgingReservationRepository;
import travel.domain.Status;
import travel.events.publish.LodgingReservationCancelled;
import travel.events.publish.LodgingReservationFailed;
import travel.events.publish.LodgingReservationRefunded;
import travel.events.subscribe.Paid;
import travel.events.subscribe.PaymentCancelled;
import travel.events.subscribe.PaymentFailed;
import travel.events.subscribe.PaymentRefundFailed;
import travel.events.subscribe.PaymentRefunded;
import travel.exception.RollBackException;

@Service
public class LodgingReservationEventService {
    @Autowired
    private LodgingReservationRepository lodgingReservationRepository;

    private static final Logger logger = LoggerFactory.getLogger("Logger");

    // paid 이벤트 수신 처리
    // 결제 완료 이벤트가 수신되었을 때 상태 변경.
    @Transactional(rollbackFor = RollBackException.class)
    public void paymentComplete(Paid paid) {
        try {
            Optional<LodgingReservation> optionalReservation = lodgingReservationRepository
                    .findById(paid.getReservationId());
            if (optionalReservation.isPresent() && "L".equals(paid.getCategory())) {
                LodgingReservation lodgingReservation = optionalReservation.get();
                lodgingReservation.setStatus(Status.예약완료);
                lodgingReservationRepository.save(lodgingReservation);
                logger.info("\nFlightReservaionId: " + paid.getReservationId() + " 결제 완료되어 예약이 확정되었습니다. \n");

            } else {
                logger.error("\n숙소 예약정보가 존재하지 않습니다. 예약 ID: {}", paid.getReservationId());
            }
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 숙소예약 상태를 변경하지 못하였습니다. \n");
            throw new RollBackException(e.getMessage());
        }

    }

    // paymentRefund 이벤트 수신 처리 + flightReservaionRefunded 이벤트 발행.
    // 결제 환불 이벤트가 수신 되었을때 해당 숙소예약 상태 변경 후 이벤트 발행
    @Transactional(rollbackFor = RollBackException.class)
    public void paymentRefund(PaymentRefunded paymentRefunded) {
        try {
            Optional<LodgingReservation> optionalReservation = lodgingReservationRepository
                    .findById(paymentRefunded.getReservationId());
            if (optionalReservation.isPresent() && "L".equals(paymentRefunded.getCategory())) {
                LodgingReservation lodgingReservation = optionalReservation.get();
                lodgingReservation.setStatus(Status.환불완료);
                lodgingReservationRepository.save(lodgingReservation);

                if (lodgingReservation.getStatus() == Status.환불완료) {
                    logger.info(
                            "\nFlightReservaionId: " + paymentRefunded.getReservationId() + " 환불 완료되어 예약이 취소되었습니다. \n");
                    LodgingReservationRefunded lodgingReservationRefunded = new LodgingReservationRefunded(
                            lodgingReservation);
                    lodgingReservationRefunded.publishAfterCommit();
                } else {
                    throw new RollBackException("예약 정보 저장 중 오류 발생");
                }
            } else {
                logger.error("\n숙소 예약정보가 존재하지 않습니다. 예약 ID: {}", paymentRefunded.getReservationId());
            }
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 숙소예약 상태를 변경하지 못하였습니다. \n");
            throw new RollBackException(e.getMessage());
        }

    }

    // paymentRefundFailed 이벤트 수신 처리
    // 결제환불 실패 이벤트가 수신되었을 때 상태 변경.
    @Transactional(rollbackFor = RollBackException.class)
    public void paymentRefundFail(PaymentRefundFailed paymentRefundFailed) {
        try {
            Optional<LodgingReservation> optionalReservation = lodgingReservationRepository
                    .findById(paymentRefundFailed.getReservationId());
            if (optionalReservation.isPresent() && "L".equals(paymentRefundFailed.getCategory())) {
                LodgingReservation lodgingReservation = optionalReservation.get();
                lodgingReservation.setStatus(Status.환불실패);
                lodgingReservationRepository.save(lodgingReservation);
                logger.info("\nLodgingReservaionId: " + paymentRefundFailed.getReservationId() + " 환불이 실패 했습니다. \n");
            } else {
                logger.error("\n숙소 예약정보가 존재하지 않습니다. 예약 ID: {}", paymentRefundFailed.getReservationId());
            }
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 숙소예약 상태를 변경하지 못하였습니다. \n");
            throw new RollBackException(e.getMessage());
        }

    }

    // paymentCancel 이벤트 수신 처리 + flightReservationCancelled 이벤트 발행
    // 결제 취소 이벤트가 수신 되었을때 해당 숙소예약 상태 변경 후 이벤트 발행
    @Transactional(rollbackFor = RollBackException.class)
    public void paymentCancel(PaymentCancelled paymentCancelled) {
        try {
            Optional<LodgingReservation> optionalReservation = lodgingReservationRepository
                    .findById(paymentCancelled.getReservationId());
            if (optionalReservation.isPresent() && "L".equals(paymentCancelled.getCategory())) {
                LodgingReservation lodgingReservation = optionalReservation.get();
                lodgingReservation.setStatus(Status.결제취소);
                lodgingReservationRepository.save(lodgingReservation);

                if (lodgingReservation.getStatus() == Status.결제취소) {
                    logger.info("\nFlightReservaionId: " + paymentCancelled.getReservationId() + " 결제를 취소 했습니다. \n");
                    LodgingReservationCancelled lodgingReservationCancelled = new LodgingReservationCancelled(
                            lodgingReservation);
                    lodgingReservationCancelled.publishAfterCommit();
                } else {
                    throw new RollBackException("예약 정보 저장 중 오류 발생");
                }
            } else {
                logger.error("\n숙소 예약정보가 존재하지 않습니다. 예약 ID: {}", paymentCancelled.getReservationId());
            }
        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 숙소예약 상태를 변경하지 못하였습니다. \n");
            throw new RollBackException(e.getMessage());
        }

    }

    // paymentFail 이벤트 수신 처리 + FlightReservationFailed 이벤트 발행
    // 결제 실패 이벤트가 수신 되었을때 해당 숙소 예약상태 변경 후 이벤트 발행
    @Transactional(rollbackFor = RollBackException.class)
    public void paymentFail(PaymentFailed paymentFailed) {
        try {
            Optional<LodgingReservation> optionalReservation = lodgingReservationRepository
                    .findById(paymentFailed.getReservationId());
            if (optionalReservation.isPresent() && "L".equals(paymentFailed.getCategory())) {
                LodgingReservation lodgingReservation = optionalReservation.get();
                lodgingReservation.setStatus(Status.결제실패);
                lodgingReservationRepository.save(lodgingReservation);

                if (lodgingReservation.getStatus() == Status.결제실패) {
                    logger.info("\nFlightReservaionId: " + paymentFailed.getReservationId() + " 결제가 실패 했습니다. \n");
                    LodgingReservationFailed LodgingReservationFailed = new LodgingReservationFailed(
                            lodgingReservation);
                    LodgingReservationFailed.publishAfterCommit();
                } else {
                    throw new RollBackException("예약 정보 저장 중 오류 발생");
                }
            } else {
                logger.error("\n숙소 예약정보가 존재하지 않습니다. 예약 ID: {}", paymentFailed.getReservationId());
            }

        } catch (Exception e) {
            logger.error("\n 알수없는 오류로 숙소예약 상태를 변경하지 못하였습니다. \n");
            throw new RollBackException(e.getMessage());
        }

    }
}
