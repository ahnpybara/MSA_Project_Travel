package travel.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;
import travel.infra.AbstractEvent;

@Data
public class SignedUp extends AbstractEvent {

    private Long id;
    private String name;
    private String password;
    private String username;
    private String refreshToken;
    private String roles;
}
