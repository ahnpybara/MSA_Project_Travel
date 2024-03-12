package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CheckPaymentDTO {
    @NotBlank
    private String merchant_uid;
    
    @NotBlank
    private String category;
}
