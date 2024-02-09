package travel.infra;

import travel.domain.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public String callApi(String depAirportId, String arrAirportId, String depPlandTime, String airlineId) {
        String url = "http://apis.data.go.kr/1613000/DmstcFlightNvgInfoService/getFlightOpratInfoList"
                + "?serviceKey=O%2B0DdibyeRPwjChz%2BqSJN%2FEurIanim0THVar8SxizDrSwO9bDs%2BJWH5YxshTVo5qudULTKjhTOOUyxZSrjD9oQ%3D%3D"
                + "&pageNo=1"
                + "&numOfRows=10"
                + "&_type=json"
                + "&depAirportId=" + depAirportId
                + "&arrAirportId=" + arrAirportId
                + "&depPlandTime=" + depPlandTime
                + "&airlineId=" + airlineId;

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
}
