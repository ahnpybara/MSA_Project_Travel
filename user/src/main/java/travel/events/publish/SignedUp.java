package travel.events.publish;

import lombok.Data;
import lombok.ToString;
import travel.domain.User;

//<<< DDD / Domain Event
@Data
@ToString
public class SignedUp extends AbstractEvent {

    private Long id;
    private String name;
    private String password;
    private String username;
    private String refreshToken;
    private String roles;

    public SignedUp(User aggregate) {
        super(aggregate);
    }

    public SignedUp() {
        super();
    }
}
//>>> DDD / Domain Event
