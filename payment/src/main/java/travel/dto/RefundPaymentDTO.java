package travel.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundPaymentDTO {

    @NotBlank(message = "\nmerchant_uid cannot be null or empty\n")
    private String merchant_uid;

    private String imp_uid; 

    private String category;
}
