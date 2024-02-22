package travel.domain;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    @NotBlank
    private String merchant_uid;

    private BigDecimal amount;

    private String imp_uid; 
}
