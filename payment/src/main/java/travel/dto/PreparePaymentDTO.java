package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class PreparePaymentDTO {
    
    @NotBlank
    private String merchant_uid;

    @NotNull
    private BigDecimal amount;
}
