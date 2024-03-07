package travel.infra;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import travel.domain.*;
import travel.external.*;
import travel.dto.*;
import travel.events.publish.FlightReservationCancelRequested;
import travel.events.publish.FlightReservationRequested;
import travel.exception.CustomException;
import travel.exception.ResponseException;
import travel.exception.RollBackException;

@Service
public class FlightReservationService {

    @Autowired
    private FlightReservationRepository flightReservationRepository;

    @Autowired
    private FlightService flightService;

    private static final Logger logger = LoggerFactory.getLogger("Logger");

    // 비행기 좌석을 가져오는 메서드
    public Long searchFlight(Long flightId) {
        try {
            ResponseEntity<Flight> flight = flightService.searchFlights(flightId);
            logger.info("\n해당하는 항공편을 찾았습니다.");
            return flight.getBody().getSeatCapacity();

        } catch (FeignException.NotFound e) {
            logger.error("\n비행 일정을 찾을 수 없습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.NOT_FOUND.value(), e.toString());
        } catch (FeignException e) {
            logger.error("\n항공 서비스 접근에 에러 발생 하였습니다.");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        }
    }

    // 비행기 좌석이 있는지 확인하는 메서드
    public void checkSeatCapacity(Long flightId) {

        Long seatCapacity = searchFlight(flightId);

        if (seatCapacity <= 0) {
            logger.error("\n좌석이 부족합니다.");
            throw new ResponseException("좌석이 부족합니다.", HttpStatus.BAD_REQUEST);
        }
        logger.info("\n좌석이 존재합니다.");
    }

    // 항공 예약 내역을 저장하는 로직
    public FlightReservation createAndSaveFlightReservation(FlightReservationDTO flightReservationDTO,
            String reservationHash) {
        try {
            FlightReservation flightReservation = new FlightReservation();
            // 속성 설정
            flightReservation.setAirLine(flightReservationDTO.getAirLine());
            flightReservation.setFlightId(flightReservationDTO.getFlightId());
            flightReservation.setArrAirport(flightReservationDTO.getArrAirport());
            flightReservation.setDepAirport(flightReservationDTO.getDepAirport());
            flightReservation.setArrTime(flightReservationDTO.getArrTime());
            flightReservation.setDepTime(flightReservationDTO.getDepTime());
            flightReservation.setCharge(flightReservationDTO.getCharge());
            flightReservation.setVihicleId(flightReservationDTO.getVihicleId());
            flightReservation.setUserId(flightReservationDTO.getUserId());
            flightReservation.setName(flightReservationDTO.getName());
            flightReservation.setReservationHash(reservationHash);
            flightReservation.setStatus(Status.결제대기);

            return flightReservationRepository.save(flightReservation);

        } catch (DataAccessException e) {
            // 데이터베이스 접근 중 예외 발생 시 사용자 정의 예외를 던짐
            throw new RollBackException("예약 정보 저장 중 오류 발생" + e.getMessage());
        }
    }

    // 예약과정을 검증하고 상태변환 시키는 비지니스 로직
    @Transactional(rollbackFor = { RollBackException.class })
    public FlightReservation validateAndProcessReservation(String reservationHash,
            FlightReservationDTO flightReservationDTO) {

        Optional<FlightReservation> existingReservation = flightReservationRepository
                .findByReservationHash(reservationHash);

        if (existingReservation.isPresent()) {
            FlightReservation existing = existingReservation.get();
            switch (existing.getStatus()) {
                case 결제대기:
                    throw new ResponseException("결제 대기중인 요청이 있습니다.", HttpStatus.CONFLICT);
                case 결제완료:
                case 예약완료:
                    throw new ResponseException("결제 완료된 예매 내역이 존재 합니다.", HttpStatus.CONFLICT);
                default:
                    // 나머지 상태들은 결제 대기로 상태 변경 후 결제요청 이벤트 발행.
                    existing.setStatus(Status.결제대기);
                    flightReservationRepository.save(existing);

                    FlightReservationRequested flightReservationRequested = new FlightReservationRequested(existing);
                    flightReservationRequested.publishAfterCommit();
                    logger.info("\n 항공예약의 상태가 결제대기로 바뀌었습니다.");
                    return existing;
            }
        } else {
            checkSeatCapacity(flightReservationDTO.getFlightId()); // 자리 확인.
            FlightReservation flightReservation = createAndSaveFlightReservation(flightReservationDTO, reservationHash);
            logger.info("\n 항공 예약이 성공적으로 생성 되었습니다.");
            FlightReservationRequested flightReservationRequested = new FlightReservationRequested(flightReservation);
            flightReservationRequested.publishAfterCommit();

            return flightReservation;
        }
    }

    // 해쉬값을 생성하는 로직
    public String createHashKey(FlightReservationDTO flightReservationDTO) throws NoSuchAlgorithmException { // 해쉬값 생성

        String input = flightReservationDTO.getUserId() + flightReservationDTO.getAirLine()
                + flightReservationDTO.getDepAirport()
                + flightReservationDTO.getArrAirport() + flightReservationDTO.getDepTime()
                + flightReservationDTO.getArrTime();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        logger.info("\n 항공예약 해쉬값 생성 완료");
        return hexString.toString();

    }

    @Transactional(rollbackFor = { RollBackException.class })
    public void cancelFlightReservation(Long reservationId) {

        try {
            Optional<FlightReservation> findReservation = flightReservationRepository.findById(reservationId);
            if (findReservation.isPresent()) {
                FlightReservation flightReservation = findReservation.get();
                if (flightReservation.getStatus() != Status.예약취소) {
                    flightReservation.setStatus(Status.예약취소);
                    flightReservationRepository.save(flightReservation);

                    FlightReservationCancelRequested flightReservationCancelRequested = new FlightReservationCancelRequested(
                            flightReservation);
                    flightReservationCancelRequested.publishAfterCommit();
                    logger.info("\n예약취소에 성공했습니다.");
                } else {
                    logger.error("\n예약 취소된 상태 입니다.");
                    throw new ResponseException("예약 취소된 상태 입니다.", HttpStatus.CONFLICT);
                }
            } else {
                logger.error("\n예약 취소 내역을 찾지 못했습니다.");
                throw new ResponseException("예약 취소 내역을 찾지 못했습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            throw new RollBackException("예약 저장중에 롤백이 발생" + e.getMessage());
        }

    }

    public FlightReservation updateReservationStatus(Long reservationId, Status newStatus) {
        // 예약 ID로 예약 객체를 찾음
        FlightReservation reservation = flightReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        // 새로운 상태로 업데이트
        reservation.setStatus(newStatus);

        // 업데이트된 예약 정보 저장 test
        return flightReservationRepository.save(reservation);
    }

}