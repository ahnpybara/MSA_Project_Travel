package travel.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import travel.auth.JwtAuthenticationFilter;
import travel.auth.PrincipalDetails;
import travel.domain.*;

@RestController
@Transactional()
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/users/register")
    public ResponseEntity<?> createUser(@RequestBody SignedUp signedUp) {
        try {
            String hashpassword = passwordEncoder.encode(signedUp.getPassword());
            User user = new User();
            user.register(signedUp);
            user.setPassword(hashpassword);
            userRepository.save(user);

            logger.info("회원가입 정보 " + user.getUsername());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            logger.error("회원가입 요청 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/users/{username}/refreshToken")
    public ResponseEntity<String> getRefresh(@PathVariable String username) {
        User user = userRepository.findByUsername(username).get();
        String token = user.getRefreshToken();
        return ResponseEntity.ok(token);
    }

    @PostMapping("/users/token/refresh")
    public ResponseEntity<?> createToken(@RequestBody String refreshToken) {
        try {
            Optional<User> optionalUser = userRepository.findByRefreshToken(refreshToken);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                PrincipalDetails principalDetails = new PrincipalDetails(user);
                String newAccessToken = JwtAuthenticationFilter.createAccessToken(principalDetails);
                return ResponseEntity.ok(newAccessToken);
            } else {
                throw new IllegalArgumentException("유효하지 않은 Token입니다.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("토큰 생성 요청 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("토큰 생성 요청 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
        }
    }

    @PostMapping("/users/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            logger.info("로그아웃 요청이 들어옴");
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
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("로그아웃 요청 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("로그아웃 요청 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
        }
    }
}