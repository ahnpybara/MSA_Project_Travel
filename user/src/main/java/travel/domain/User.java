package travel.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.Builder;
import lombok.Data;
import travel.UserApplication;
import travel.domain.LoggedIn;
import travel.domain.SignedUp;

@Entity
@Table(name = "User_table")
@Data
// <<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String username;

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
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashPassword = passwordEncoder.encode(signedUp.getPassword());

        validateDuplicate(signedUp.getUsername());

        setName(signedUp.getName());
        setPassword(hashPassword);
        setUsername(signedUp.getUsername());

        if (signedUp.getUsername().equals("admin")) {
            setRoles("ROLE_ADMIN,ROLE_USER");
        } else {
            setRoles("ROLE_USER");
        }
    }
    //중복 검사
    public void validateDuplicate(String username){
        Optional<User> optionalUser = repository().findByUsername(username);
        if(optionalUser.isPresent()){
            throw new IllegalArgumentException("이미 사용 중인 아이디 입니다.");
        }
    }
}
// >>> DDD / Aggregate Root
