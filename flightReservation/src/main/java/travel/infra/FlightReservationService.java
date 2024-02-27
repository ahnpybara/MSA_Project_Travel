package travel.infra;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import travel.domain.*;
import travel.external.*;
import travel.external.Flight;
@Service
public class FlightReservationService {
    
    @Autowired
    private FlightReservationRepository flightReservationRepository;

    @Autowired
    private FlightService flightService;


    // 비행기 좌석을 가져오는 로직
    public Long searchFlight(Long flightId){
        try {
            ResponseEntity<Flight> flight = flightService.searchFlights(flightId); 
            return flight.getBody().getSeatCapacity();
        } catch (FeignException.NotFound e) {
            System.out.println("비행 일정을 찾을수 없습니다.");
            throw new RollBackException("비행 일정을 찾을수 없습니다.");
        } catch ( FeignException e){
            System.out.println("항공 서비스 접근에 에러 발생 하였습니다.");
            throw new RollBackException("항공 서비스 접근에 에러 발생 하였습니다.");
        }
    }
    public void checkSeatCapacity(Long flightId){
    
            Long seatCapacity = searchFlight(flightId);
        
            if (seatCapacity <= 0){
                System.out.println("좌석이 부족합니다.");
                throw new RollBackException("좌석이 부족합니다..");
            }
        
    }


    //예약과정을 검증하고 상태변환 시키는  비지니스 로직
    @Transactional(rollbackFor = {RollBackException.class})
    public FlightReservation validateAndProcessReservation(String reservationHash, FlightReservationDTO flightReservationDTO) throws ResponseStatusException {
    try {
        Optional<FlightReservation> existingReservation = flightReservationRepository.findByReservationHash(reservationHash);
    if (existingReservation.isPresent()) {
        FlightReservation existing = existingReservation.get();
        switch (existing.getStatus()) {
            case 결제대기:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "결제 대기중인 요청이 있습니다.");
            case 결제완료:
            case 예약완료:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "결제 완료된 예매 내역이 존재 합니다.");
            default:
                // 나머지 상태들은 결제 대기로 상태 변경후 결제요청 이벤트 발행.
                existing.setStatus(Status.결제대기);
                flightReservationRepository.save(existing);

                PaymentRequested paymentRequested = new PaymentRequested(existing);
                paymentRequested.publishAfterCommit();
                return existing;
         }
    } else {
            checkSeatCapacity(flightReservationDTO.getFlightId());           // 자리 확인.
            FlightReservation flightReservation = new FlightReservation();
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
            flightReservation.setFlightId(flightReservationDTO.getFlightId());
            flightReservationRepository.save(flightReservation);
            
            PaymentRequested paymentRequested = new PaymentRequested(flightReservation);
            paymentRequested.publishAfterCommit();
            return flightReservation;
        }
     } catch (ResponseStatusException e){
            throw new ResponseStatusException(e.getStatus(),e.getMessage());
     }
        catch (Exception e){
            throw new RollBackException("예약 저장중에 롤백이 발생" + e.getMessage());
        }

    }

    
    // 해쉬값을 생성하는 로직
    public String createHashKey(FlightReservationDTO flightReservationDTO) throws NoSuchAlgorithmException {           // 해쉬값 생성
        
        
        String input = flightReservationDTO.getUserId() + flightReservationDTO.getAirLine() + flightReservationDTO.getDepAirport()
                    + flightReservationDTO.getArrAirport() + flightReservationDTO.getDepTime() + flightReservationDTO.getArrTime();
        
        
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for( byte b : hash){
            String hex = Integer.toHexString(0xff & b);
            if(hex.length()==1)hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Transactional(rollbackFor = {RollBackException.class})
    public  void cancelFlightReservation(Long reservationId){

        try {
            Optional<FlightReservation> findReservation = flightReservationRepository.findById(reservationId);
            if(findReservation.isPresent()){
            FlightReservation flightReservation = findReservation.get();
            flightReservation.setStatus(Status.예약취소);
            flightReservationRepository.save(flightReservation);

            FlightCancelRequest flightCancelRequest = new FlightCancelRequest(flightReservation);
            flightCancelRequest.publishAfterCommit();
            }
            else{
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "예약 취소 내역을 찾지 못했습니다.");
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