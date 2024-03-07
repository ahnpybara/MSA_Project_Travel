package travel.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class FailPaymentDTO {

    @NotBlank
    private String merchant_uid;
}
