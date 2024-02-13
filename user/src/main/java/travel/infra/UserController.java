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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import travel.auth.JwtAuthenticationFilter;
import travel.auth.PrincipalDetails;
import travel.domain.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/users")
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
