    package travel.infra;

    import java.util.Optional;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.transaction.Transactional;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

import travel.auth.JwtAuthenticationFilter;
import travel.auth.PrincipalDetails;
import travel.domain.*;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;


    //<<< Clean Arch / Inbound Adaptor

    @RestController
    //@RequestMapping(value="/users")
    @Transactional()
    public class UserController {

        @Autowired
        private UserRepository userRepository;


        @PostMapping("/users/register")
        public ResponseEntity<User> createUser(@RequestBody SignedUp signedUp) {
            User user = new User();
            user.register(signedUp);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        
        @PostMapping("/users/token/refresh")
        public ResponseEntity<String> createToken(@RequestBody String refreshToken){
            Optional<User> optionalUser = userRepository.findByRefreshToken(refreshToken);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                //사용자 인증 정보 클래스 객체 생성
                PrincipalDetails principalDetails = new PrincipalDetails(user);
                                //accessToken 발급
                JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(null, null, null);
                String newAccessToken = jwtAuthenticationFilter.createAccessToken(principalDetails);
                return ResponseEntity.ok(newAccessToken);
            } else{
                return ResponseEntity.ok("refresh Token 불일치");
            }
            
        }
    }
    //>>> Clean Arch / Inbound Adaptor
