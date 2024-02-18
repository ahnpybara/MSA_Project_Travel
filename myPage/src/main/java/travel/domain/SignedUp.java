package travel.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class SignedUp extends AbstractEvent {

    private Long id;
    private String name;
    private String password;
    private String username;
    private String refreshToken;
    private String roles;
}
