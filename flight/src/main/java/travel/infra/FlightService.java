package travel.infra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import travel.domain.Flight;
import travel.domain.FlightRepository;
import travel.event.subscribe.FlightReservationCancelled;
import travel.event.subscribe.FlightReservationRequested;
import travel.exception.CustomException;
import travel.exception.RollbackException;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    private Map<String, String> airportIdToNmMap;

    private static final Logger logger = LoggerFactory.getLogger("MyLogger");

    // 공항의 ID와 공항의 이름을 연결하는 맵을 선언합니다.
    @PostConstruct
    public void init() {
        airportIdToNmMap = new HashMap<>();
        airportIdToNmMap.put("NAARKJB", "무안");
        airportIdToNmMap.put("NAARKJJ", "광주");
        airportIdToNmMap.put("NAARKJK", "군산");
        airportIdToNmMap.put("NAARKJY", "여수");
        airportIdToNmMap.put("NAARKNW", "원주");
        airportIdToNmMap.put("NAARKNY", "양양");
        airportIdToNmMap.put("NAARKPC", "제주");
        airportIdToNmMap.put("NAARKPK", "김해");
        airportIdToNmMap.put("NAARKPS", "사천");
        airportIdToNmMap.put("NAARKPU", "울산");
        airportIdToNmMap.put("NAARKSI", "인천");
        airportIdToNmMap.put("NAARKSS", "김포");
        airportIdToNmMap.put("NAARKTH", "포항");
        airportIdToNmMap.put("NAARKTN", "대구");
        airportIdToNmMap.put("NAARKTU", "청주");
    }

    // 공항의 ID를 입력받아 해당하는 공항의 이름을 반환하는 메서드입니다
    private String convertAirportIdToName(String airportId) {
        return Optional.ofNullable(airportIdToNmMap.get(airportId))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 공항ID 입니다."));
    }

    // 사용자가 요청한 조건에 맞는 항공편을 찾아 리스트로 반환하는 메서드입니다
    public List<Flight> findFlights(String depAirportId, String arrAirportId, Long startTimestamp, Long endTimestamp) {

        try {
            String depAirportNm = convertAirportIdToName(depAirportId);
            String arrAirportNm = convertAirportIdToName(arrAirportId);
            return flightRepository.findByDepAirportNmAndArrAirportNmAndDepPlandTimeBetweenAndSeatCapacityGreaterThanEqual(
                    depAirportNm, arrAirportNm, startTimestamp, endTimestamp, 0L);
        } catch (IllegalArgumentException e) {
            logger.error("\n유효하지 않은 공항Id 입니다\n");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n항공편의 정보를 DB에서 찾는 도중 문제가 발생했습니다.\n 오류 내용 : " + e + "\n");
            throw new CustomException("항공편의 정보를 DB에서 찾는 도중 문제가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }

    // 예약 요청이 되었을 때 해당 항공편의 좌석수를 감소
    @Transactional(rollbackFor = RollbackException.class)
    public void decreaseSeatCapacity(FlightReservationRequested flightReservationRequested) {
        try {
            Flight flight = flightRepository.findById(flightReservationRequested.getFlightId())
                    .orElseThrow(() -> new IllegalArgumentException("예약 요청된 항공편의 정보를 찾을 수 없습니다"));
            if (flight.getSeatCapacity() <= 0)
                throw new IllegalStateException("해당 항공편의 좌석수가 부족합니다");
            flight.setSeatCapacity(flight.getSeatCapacity() - 1);
            flightRepository.save(flight);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("\n예약된 항공편을 찾을 수 없거나, 항공편의 남은 좌석수가 부족합니다.\n");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n알 수 없는 오류로 해당 항공편의 좌석수를 감소하는데 실패했습니다\n");
            throw new RollbackException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }


    // 예약 요청이 최소 되었을 때 해당 항공편의 좌석수를 증가
    @Transactional(rollbackFor = RollbackException.class)
    public void increaseSeatCapacity(FlightReservationCancelled flightReservationCancelled) {
        try {
            Flight flight = flightRepository.findById(flightReservationCancelled.getFlightId())
                    .orElseThrow(() -> new IllegalArgumentException("예약 요청된 항공편의 정보를 찾을 수 없습니다"));
            if (flight.getSeatCapacity() <= 0)
                throw new IllegalStateException("해당 항공편의 좌석수가 이미 MAX 입니다");
            flight.setSeatCapacity(flight.getSeatCapacity() + 1);
            flightRepository.save(flight);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("\n예약된 항공편을 찾을 수 없거나, 항공편의 좌석수가 이미 MAX입니다\n");
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e.toString());
        } catch (Exception e) {
            logger.error("\n알 수 없는 오류로 해당 항공편의 좌석수를 증가하는데 실패했습니다\n");
            throw new RollbackException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.toString());
        }
    }
    
    // 항공예약 서비스로 부터 특정 항공편의 좌석수를 구하기 위해 사용될 API
    public Flight findFlightAPI(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청된 Id를 가진 항공편이 존재하지 않습니다"));
    }
}
