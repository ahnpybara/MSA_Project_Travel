package travel.infra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import travel.domain.*;

@RestController
@Transactional
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/users/register")
    public ResponseEntity<?> createUser(@RequestBody SignedUp signedUp) {
        logger.info("회원가입 시도");
        User user = userService.save(signedUp);
        logger.info("회원가입 완료");
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{username}/refreshToken")
    public ResponseEntity<String> getRefresh(@PathVariable String username) {
        String token=userService.getRefreshToken(username);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/users/token/refresh")
    public ResponseEntity<?> createToken(@RequestBody String refreshToken) {
        String newAccessToken=userService.createToken(refreshToken);
        return ResponseEntity.ok(newAccessToken);
    }

    @PostMapping("/users/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        userService.logout(request);
        logger.info("로그아웃 완료");
        return ResponseEntity.ok("로그아웃 성공");
    }
}
