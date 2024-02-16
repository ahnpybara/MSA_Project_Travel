package travel.auth;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import travel.domain.User;
import travel.domain.UserRepository;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {// 생성자
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        // 로그인 주소를 변경합니다.
        this.setFilterProcessesUrl("/users/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter :로그인 시도중");
        try {
            ObjectMapper om = new ObjectMapper();// 객체 매핑
            User user = om.readValue(request.getInputStream(), User.class);
            logger.info(user);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), user.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            logger.info(principalDetails.getUser().getUsername());

            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        System.out.println("인증이 무사히 성공됨");

        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // access 토큰 발급
        String accessToken = createAccessToken(principalDetails);
        logger.info("엑세스 토큰=========================="+accessToken);
        // refresh 토큰 발급
        String refreshToken = createRefreshToken(principalDetails);
        logger.info("리프레쉬 토큰=========================="+refreshToken);
        // 리프레시 토큰으로 사용자 업데이트
        Optional<User> optionalUser = userRepository.findByUsername(principalDetails.getUser().getUsername());
        optionalUser.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });

        // 헤더에 토큰 추가
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);
        response.setHeader("Access-Control-Expose-Headers", JwtProperties.HEADER_STRING);
        logger.info("로그인 완료");
    }

    // access 토큰 생성 메소드
    public static String createAccessToken(PrincipalDetails principalDetails) {
        String accessToken = JWT.create()
                .withSubject(JwtProperties.TOKENNAME)
                .withExpiresAt(Date.from(Instant.now().plus(JwtProperties.AJ_TIME, ChronoUnit.MINUTES)))
                .withClaim("id", principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                .withClaim("roles", principalDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(String::trim)
                        .collect(Collectors.toList()))
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return accessToken;
    }

    public String createRefreshToken(PrincipalDetails principalDetails) {
        // refresh 토큰 발급
        String refreshToken = JWT.create()
                .withSubject(JwtProperties.TOKENNAME)
                .withExpiresAt(Date.from(Instant.now().plus(JwtProperties.RT_TIME, ChronoUnit.MINUTES)))
                .withClaim("id", principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
        return refreshToken;
    }
}