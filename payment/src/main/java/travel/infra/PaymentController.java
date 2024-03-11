package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import travel.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/payments")
@Transactional
public class PaymentController {

    @Autowired
    PaymentRepository paymentRepository;


@PostMapping("/test")
public void checkEvent(@RequestBody Paid newpaid) {
    Paid paid = new Paid();
    paid.setReservationId(newpaid.getReservationId());
    paid.publish();
}

@PostMapping("/test1")
public void checkEvent(@RequestBody PaymentRefunded paymentRefunded1) {
    PaymentRefunded paymentRefunded = new PaymentRefunded();
    paymentRefunded.setReservationId(paymentRefunded1.getReservationId());
    paymentRefunded.publish();
}

@PostMapping("/test2")
public void checkEvent(@RequestBody PaymentRefundFailed paymentRefunded1) {
    PaymentRefundFailed paymentRefunded = new PaymentRefundFailed();
    paymentRefunded.setReservationId(paymentRefunded1.getReservationId());
    paymentRefunded.publish();
}

@PostMapping("/test3")
public void checkEvent(@RequestBody PaymentCancelled paymentRefunded1) {
    PaymentCancelled paymentRefunded = new PaymentCancelled();
    paymentRefunded.setReservationId(paymentRefunded1.getReservationId());
    paymentRefunded.publish();
}

@PostMapping("/test4")
public void checkEvent(@RequestBody PaymentFailed paymentRefunded1) {
    PaymentFailed paymentRefunded = new PaymentFailed();
    paymentRefunded.setReservationId(paymentRefunded1.getReservationId());
    paymentRefunded.publish();
}

}
//>>> Clean Arch / Inbound Adaptor
