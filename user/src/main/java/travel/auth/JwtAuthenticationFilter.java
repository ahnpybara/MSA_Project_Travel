package travel.auth;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
        // 로그인 주소를 변경합니다
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
            throw new RuntimeException("로그인 정보 읽기 실패", e);
        } catch (AuthenticationException e) {
            logger.info("아이디가 존재하지 않거나 비밀번호가 일치하지 않음");
            sendErrorMessage(response, "아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.");
            return null;
        } catch (JWTVerificationException e) {
            logger.info("사용자 인증 실패로 토큰 생성에 실패");
            sendErrorMessage(response, "토큰 생성에 실패했습니다.");
            return null;
        }
    }

    //응답 메세지 생성 메소드
    private void sendErrorMessage(HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = String.format("{\"message\": \"%s\"}", message);
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        logger.info("로그인 아이디와 비밀번호 인증 성공");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // access 토큰 발급
        String accessToken = createAccessToken(principalDetails);
        logger.info("엑세스 토큰 발급");
        // refresh 토큰 발급
        String refreshToken = createRefreshToken(principalDetails);
        logger.info("리프레쉬 토큰 발급");
        // 리프레시 토큰으로 사용자 업데이트
        Optional<User> optionalUser = userRepository.findByUsername(principalDetails.getUser().getUsername());
        optionalUser.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });

        // 헤더에 토큰 추가
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);
        response.setHeader("Access-Control-Expose-Headers", JwtProperties.HEADER_STRING);

        // 응답 데이터에 ID 추가
        Map<String, String> data = new HashMap<>();
        data.put("userId", principalDetails.getUser().getId().toString());
        data.put("name",principalDetails.getUser().getName());
        data.put("username", principalDetails.getUser().getUsername());
        // JSON 형식으로 응답 작성
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), data);
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