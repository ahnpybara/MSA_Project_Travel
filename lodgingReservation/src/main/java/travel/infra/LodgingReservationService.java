package travel.infra;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;
import travel.domain.LodgingReservation;
import travel.domain.LodgingReservationRepository;
import travel.domain.Status;
import travel.dto.LodgingReservationDTO;
import travel.events.publish.LodgingReservationCancelRequested;
import travel.events.publish.LodgingReservationRequested;
import travel.exception.CustomException;
import travel.exception.RollBackException;
import travel.external.RoomService;

@Service
public class LodgingReservationService {

    @Autowired
    private LodgingReservationRepository lodgingReservationRepository;

    @Autowired
    private RoomService roomService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");

    @Transactional(rollbackFor = { RollBackException.class })
    public LodgingReservation validateAndProcessReservation(LodgingReservationDTO lodgingReservationDTO) {

        Optional<LodgingReservation> existingReservation = lodgingReservationRepository
                .findByNameAndReservationDateAndEmailAndRoomcode(
                        lodgingReservationDTO.getName(), lodgingReservationDTO.getReservationDate(),
                        lodgingReservationDTO.getEmail(), lodgingReservationDTO.getRoomcode());

        if (existingReservation.isPresent()) {
            LodgingReservation existing = existingReservation.get();
            if (existing.getStatus() == Status.결제대기) {
                logger.info("\n결제 대기중인 요청이 있습니다.\n");
                throw new CustomException("결제 대기중인 요청이 있습니다.", HttpStatus.CONFLICT.value());
            } else if (existing.getStatus() == Status.결제완료 || existing.getStatus() == Status.예약완료) {
                logger.info("\n결제 완료 및 예약완료된 예약 내역이 존재 합니다.\n");
                throw new CustomException("결제 완료 및 예약완료된 예약 내역이 존재 합니다.", HttpStatus.CONFLICT.value());
            } else {
                checkRoomCapacity(lodgingReservationDTO.getRoomcode(), lodgingReservationDTO.getReservationDate());
                existing.setStatus(Status.결제대기);
                lodgingReservationRepository.save(existing);
                if (existing.getStatus() == Status.결제대기) {
                    LodgingReservationRequested lodgingReservationRequested = new LodgingReservationRequested(existing);
                    lodgingReservationRequested.publishAfterCommit();
                } else {
                    throw new RollBackException("예약 정보 저장중 오류 발생");
                }
                logger.info("\n 숙소예약의 상태가 결제 대기로 바뀌었습니다. \n");
                return existing;
            }
        }
        checkRoomCapacity(lodgingReservationDTO.getRoomcode(), lodgingReservationDTO.getReservationDate());
        LodgingReservation lodgingReservation = createAndSaveLodgingReservation(lodgingReservationDTO);
        logger.info("\n 숙소 예약이 성공적으로 생성 되었습니다. \n");
        LodgingReservationRequested lodgingReservationRequested = new LodgingReservationRequested(lodgingReservation);
        lodgingReservationRequested.publishAfterCommit();

        return lodgingReservation;

    }

    public LodgingReservation createAndSaveLodgingReservation(LodgingReservationDTO lodgingReservationDTO) {

        try {
            LodgingReservation lodgingReservation = new LodgingReservation();
            lodgingReservation.setName(lodgingReservationDTO.getName()); 
            lodgingReservation.setReservationDate(lodgingReservationDTO.getReservationDate());
            lodgingReservation.setEmail(lodgingReservationDTO.getEmail());
            lodgingReservation.setCharge(lodgingReservationDTO.getCharge());
            lodgingReservation.setRoomcode(lodgingReservationDTO.getRoomcode());
            lodgingReservation.setUserId(lodgingReservationDTO.getUserId());
            lodgingReservation.setCategory("L");
            lodgingReservation.setStatus(Status.결제대기);

            return lodgingReservationRepository.save(lodgingReservation);
        } catch (Exception e) {
            logger.error("\n 숙소 예약 저장 중 롤백 발생");
            throw new RollBackException("예약 정보 저장 중 오류 발생 " + e.getMessage());
        }
    }

    public Long searchFlight(Long roomCode, Long reservationDate) {
        try {

            ResponseEntity<Long> room = roomService.searchRooms(roomCode, reservationDate); 
                                                                                            
            logger.info("\n해당하는 숙소를 찾았습니다. \n");
            return room.getBody().longValue();

        } catch (FeignException.NotFound e) {
            logger.error("\n숙소를 을 찾을 수 없습니다. \n");
            throw new CustomException(e.getMessage(), HttpStatus.NOT_FOUND.value(), e.toString());
        } catch (FeignException e) {
            logger.error("\n숙소 서비스 접근에 에러 발생 하였습니다. \n");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        }
    }

    // 해당 숙소편 좌석이 있는지 확인하는 메서드
    public void checkRoomCapacity(Long roomCode, Long reservationDate) {

        Long roomCapacity = searchFlight(roomCode, reservationDate);

        if (roomCapacity <= 0) {
            logger.error("\n객실이 부족합니다. \n");
            throw new CustomException("객실이 부족합니다.", HttpStatus.BAD_REQUEST.value());
        }
        logger.info("\n객실이 존재합니다. \n");
    }

    @Transactional(rollbackFor = { RollBackException.class })
    public void cancelLodgingReservation(Long reservationId) {

        try {
            Optional<LodgingReservation> findReservation = lodgingReservationRepository.findById(reservationId);
            if (findReservation.isPresent()) {
                LodgingReservation lodgingReservation = findReservation.get();
                if (lodgingReservation.getStatus() == Status.결제대기) {
                    lodgingReservation.setStatus(Status.예약취소);
                    lodgingReservationRepository.save(lodgingReservation);

                    if (lodgingReservation.getStatus() == Status.예약취소) {
                        LodgingReservationCancelRequested lodgingReservationCancelRequested = new LodgingReservationCancelRequested(
                                lodgingReservation);
                        lodgingReservationCancelRequested.publishAfterCommit();
                        logger.info("\n예약취소에 성공했습니다.\n");
                    } else {
                        throw new RollBackException("예약 저장중에 롤백이 발생");
                    }
                } else if (lodgingReservation.getStatus() == Status.예약취소) {
                    logger.error("\n예약 취소된 상태 입니다.\n");
                    throw new CustomException("예약 취소된 상태 입니다.", HttpStatus.CONFLICT.value());
                } else {
                    logger.error("\n현재 상태는 예약 취소를 할 수 없습니다\n");
                    throw new CustomException("현재 상태는 예약 취소를 할 수 없습니다", HttpStatus.BAD_REQUEST.value());
                }
            } else {
                logger.error("\n예약 취소 내역을 찾지 못했습니다.\n");
                throw new CustomException("예약 취소 내역을 찾지 못했습니다.", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            throw new RollBackException(e.getMessage());
        }

    }
}
