package travel.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundPaymentDTO {

    @NotBlank
    private String merchant_uid;

    private String imp_uid;

    @NotBlank
    private String category;
}
