package travel.domain;

import lombok.Data;
import lombok.ToString;
import travel.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
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
//>>> DDD / Domain Event
