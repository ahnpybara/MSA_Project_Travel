package travel.infra;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import net.bytebuddy.implementation.bytecode.Throw;
import travel.domain.*;

@Service
public class FlightReservationService {
    
    @Autowired
    private FlightReservationRepository flightReservationRepository;

    @Autowired
    private Scheduler scheduler;


    public FlightReservation saveFlightReservation(FlightReservation flightReservation){            // 예약 저장 로직
        return flightReservationRepository.save(flightReservation);
    }
    
public void validateAndProcessReservation(String reservationHash, FlightReservation flightReservation) throws ResponseStatusException {
    Optional<FlightReservation> existingReservation = flightReservationRepository.findByReservationHash(reservationHash);
    
    if (existingReservation.isPresent()) {
        FlightReservation existing = existingReservation.get();
        switch (existing.getStatus()) {
            case 결제대기:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "결제 대기중인 요청이 있습니다.");
            case 결제완료:
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예매 내역이 존재 합니다.");
            case 취소완료:
                // 취소된 예약에 대한 처리: 상태 변경 또는 다른 처리
                existing.setStatus(Status.결제대기);
                flightReservationRepository.save(existing);
                throw new ResponseStatusException(HttpStatus.OK, "취소 완료된 예약이 재활성화되었습니다.");
        }
    } else {
        // 새 예약 처리 로직
        flightReservation.setReservationHash(reservationHash);
        flightReservation.setStatus(Status.결제대기);
        flightReservationRepository.save(flightReservation);
    }
}

    
    
    public String createHashKey(FlightReservation flightReservation) throws NoSuchAlgorithmException {           // 해쉬값 생성
        
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
        
        // 업데이트된 예약 정보 저장
        return flightReservationRepository.save(reservation);
    }
}
 
