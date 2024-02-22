package travel.domain;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "UserInfo_table")
@Data
public class UserInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String name;
    private String username;
    private String password;
    private String refreshToken;
    private String roles;
}
