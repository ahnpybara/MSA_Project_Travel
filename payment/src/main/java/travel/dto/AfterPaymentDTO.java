package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AfterPaymentDTO {
    @NotBlank(message = "\nmerchant_uid cannot be null or empty\n")
    private String merchant_uid;

    @NotBlank(message = "\nimp_uid cannot be null or empty\n")
    private String imp_uid; 

    @NotBlank(message = "\ncategory cannot be null or empty\n")
    private String category;
}
