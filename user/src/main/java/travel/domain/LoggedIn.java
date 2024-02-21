package travel.domain;

import lombok.*;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString
public class LoggedIn extends AbstractEvent {
//.
    private Long id;
    private String password;
    private String name;
    private String username;
    private String refreshToken;
    private String roles;

    public LoggedIn(User aggregate) {
        super(aggregate);
    }

    public LoggedIn() {
        super();
    }
}
