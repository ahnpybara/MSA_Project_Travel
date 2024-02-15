package travel.domain;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private String merchant_uid;
    private BigDecimal amount;
    private String imp_uid;
}