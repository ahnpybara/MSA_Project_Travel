package travel.external;

import java.util.Date;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "lodging", url = "${api.url.lodging}")
public interface RoomService {
    @GetMapping(path = "/rooms")
    public List<Room> getRoom();

    @GetMapping(path = "/rooms/{id}")
    public Room getRoom(@PathVariable("id") Long id);

    @GetMapping(path = "/rooms/roomCapacity")
    public ResponseEntity<Room> searchRooms (@RequestParam("roomCode") Long roomCode , @RequestParam("reservationDate") Long ReservationDate);
}
