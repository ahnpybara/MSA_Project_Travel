package travel.infra;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import net.bytebuddy.implementation.bytecode.Throw;
import travel.domain.*;
import travel.external.*;
@Service
public class FlightReservationService {
    
    @Autowired
    private FlightReservationRepository flightReservationRepository;

/*     @Autowired
    private FlightService flightService;


    // 비행기 좌석을 가져오는 로직
    public Long searchFlights(String airLine, String arrAirport , String depAirpost, String vihicleId){
        try {
            Flight flight = flightService.searchFlights(airLine, arrAirport, depAirpost, vihicleId); 
            return flight.getSeatCapacity();
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "비행 일정을 찾을수 없습니다.");
        } catch ( FeignException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error accessing flight service");
        }
    }
    public void checkSeatCapacity(FlightReservation flightReservation){
        
        Long seatCapacity =searchFlights(flightReservation.getAirLine(), flightReservation.getArrAirport(), flightReservation.getDepAirport(), flightReservation.getVihicleId());
        
        if (seatCapacity <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "좌석이 부족합니다.");
        }
        
    }
 */
   
    //예약과정을 검증하고 상태변환 시키는  비지니스 로직
    public void validateAndProcessReservation(String reservationHash, FlightReservation flightReservation) throws ResponseStatusException {
    Optional<FlightReservation> existingReservation = flightReservationRepository.findByReservationHash(reservationHash);
    
    if (existingReservation.isPresent()) {
        FlightReservation existing = existingReservation.get();
        switch (existing.getStatus()) {
            case 결제대기:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "결제 대기중인 요청이 있습니다.");
            case 결제완료:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "결제 완료된 예매 내역이 존재 합니다.");
            case 취소완료:
                // 취소된 예약에 대한 처리
                try {
                    existing.setStatus(Status.결제대기);
                    flightReservationRepository.save(existing);
                    throw new RollBackException("항공편 저장이 제대로 수행되지 않음 롤백");
                    //throw new ResponseStatusException(HttpStatus.OK, "취소 완료된 예약이 재활성화되었습니다.");
                } catch (RollBackException e) {
                    throw new RollBackException("항공편 저장이 제대로 수행되지 않음 롤백");
                }
         }
    } else {
            //checkSeatCapacity(flightReservation);           // 자리 확인.
        // 새 예약 처리 로직
        try {
            flightReservation.setReservationHash(reservationHash);
            flightReservation.setStatus(Status.결제대기);
            flightReservationRepository.save(flightReservation);

        } catch (RollBackException e) {
            throw new RollBackException("항공편 저장이 제대로 수행되지 않음 롤백");
        }
    }
}
    public void validateInput(FlightReservation flightReservation){
        if(flightReservation.getUserId() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId 값이 없습니다."); 
        }
        else if(flightReservation.getAirLine() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AirLine 값이 없습니다."); 
        }
        else if(flightReservation.getDepAirport() == null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DepAirport 값이 없습니다."); 
        }
        else if(flightReservation.getDepTime() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DepTime 값이 없습니다.");
        }
        else if(flightReservation.getArrTime() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ArrTime 값이 없습니다.");
        }
        else if(flightReservation.getArrAirport() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ArrAirport 값이 없습니다.");
        }
        else if(flightReservation.getDepAirport() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DepAirport 값이 없습니다.");
        }
        else if(flightReservation.getCharge() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "charge 값이 없습니다.");
        }
        else if(flightReservation.getVihicleId() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VihicleId 값이 없습니다.");
        }
    }
    
    // 해쉬값을 생성하는 로직
    public String createHashKey(FlightReservation flightReservation) throws NoSuchAlgorithmException {           // 해쉬값 생성
        
        validateInput(flightReservation);

        String input = flightReservation.getUserId() + flightReservation.getAirLine() + flightReservation.getDepAirport()
                    + flightReservation.getArrAirport() + flightReservation.getDepTime() + flightReservation.getArrTime();
        
        
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