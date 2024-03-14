package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class PreparePaymentDTO {
    
    @NotNull(message = "\namount cannot be null or empty\n")
    private BigDecimal amount;

    @NotBlank(message = "\nmerchant_uid cannot be null or empty\n")
    private String merchant_uid;

    @NotBlank(message = "\ncategory cannot be null or empty\n")
    private String category;
}
