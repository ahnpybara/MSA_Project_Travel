package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AfterPaymentDTO {
    @NotBlank
    private String merchant_uid;

    @NotBlank
    private String imp_uid; 

    @NotBlank
    private String category;
}
