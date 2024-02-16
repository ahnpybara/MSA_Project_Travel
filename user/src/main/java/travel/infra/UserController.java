package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import travel.auth.JwtAuthenticationFilter;
import travel.auth.PrincipalDetails;
import travel.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/users")
@Transactional()
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/users/register")
    public ResponseEntity<User> createUser(@RequestBody SignedUp signedUp) {
        System.out.println("회원가입 진행 중");
        String hashpassword = passwordEncoder.encode(signedUp.getPassword());
        User user = new User();
        user.register(signedUp);
        user.setPassword(hashpassword);
        userRepository.save(user);

        System.out.println("회원가입 정보 " + user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{username}/refreshToken")
    public ResponseEntity<String> getRefresh(@PathVariable String username) {
        User user = userRepository.findByUsername(username).get();
        String token = user.getRefreshToken();
        return ResponseEntity.ok(token);
    }

    @PostMapping("/users/token/refresh")
    public ResponseEntity<String> createToken(@RequestBody String refreshToken) {
        Optional<User> optionalUser = userRepository.findByRefreshToken(refreshToken);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 사용자 인증 정보 클래스 객체 생성
            PrincipalDetails principalDetails = new PrincipalDetails(user);
            // accessToken 발급
            String newAccessToken = JwtAuthenticationFilter.createAccessToken(principalDetails);
            return ResponseEntity.ok(newAccessToken);
        } else {
            return ResponseEntity.ok("refresh Token 불일치");
        }

    }

    @PostMapping("/users/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        System.out.println("로그아웃 요청이 들어옴");
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        String username = decodedJWT.getClaim("username").asString();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRefreshToken(null);
            userRepository.save(user);
            return ResponseEntity.ok("로그아웃 성공");
        } else {
            return ResponseEntity.ok("토큰이 일치하지 않음");
        }
    }

}
// >>> Clean Arch / Inbound Adaptor
