package travel.dto;

import lombok.Data;

@Data
public class SignedUpDTO {
    private Long id;
    private String name;
    private String password;
    private String username;
    private String refreshToken;
    private String roles;
    
}
