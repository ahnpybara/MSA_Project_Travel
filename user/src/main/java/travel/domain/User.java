package travel.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.UserApplication;

@Entity
@Table(name = "User_table")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String username;

    private String nickname;

    private String password;

    private String refreshToken;

    private String roles;

    public List<String> getRoleList() {
        if (this.roles.length() > 0) {
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();
    }

    @PostPersist
    public void onPostPersist() {
        SignedUp signedUp = new SignedUp(this);
        signedUp.publishAfterCommit();

        LoggedIn loggedIn = new LoggedIn(this);
        loggedIn.publishAfterCommit();
    }

    public static UserRepository repository() {
        UserRepository userRepository = UserApplication.applicationContext.getBean(
                UserRepository.class);
        return userRepository;
    }

    public void register(SignedUp signedUp) {
        setName(signedUp.getName());
        setPassword(signedUp.getPassword());
        setUsername(signedUp.getUsername());

        if (signedUp.getUsername().equals("admin")) {
            setRoles("ROLE_ADMIN,ROLE_USER");
        } else {
            setRoles("ROLE_USER");
        }
    }
   
}
