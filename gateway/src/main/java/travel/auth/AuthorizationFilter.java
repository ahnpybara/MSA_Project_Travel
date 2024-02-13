package travel.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authorizationHeader.substring(7);

            if (isTokenExpired(token)) {
                DecodedJWT decodedJWT = JWT.decode(token);
                String username = decodedJWT.getClaim("username").asString();
                String refreshToken = getRefreshToken(username);
                String newToken = requestNewToken(refreshToken);

                exchange.getResponse().getHeaders().add("newauthorization", "Bearer " + newToken);
            }

            return chain.filter(exchange);
        };
    }

    private boolean isTokenExpired(String token) {
        try {
            token = token.substring(7);

            JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()))
                    .build()
                    .verify(token)// 검증
                    .getClaims();// 검증된 토큰의 claims를 가져옴

            return false;
        } catch (TokenExpiredException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    // 리프레쉬 토큰 알아내기 위한 것
    private String getRefreshToken(String username) {
        WebClient webClient = WebClient.create("http://localhost:8088");// 요청 주소
        String refreshToken = webClient.get()
                .uri("/users/{username}/refreshToken", username) // 상세 주소,
                .retrieve()
                .bodyToMono(String.class)//모노 사용, 받는 데이터의 타입을 Sring으로 받기
                .block();

        return refreshToken;
    }

    // 새로운 엑세스 토큰 발급을 위한 것
    private String requestNewToken(String refreshToken) {
        WebClient webClient = WebClient.create("http://localhost:8088");
        String newToken = webClient.post()
                .uri("/users/token/refresh")
                .body(BodyInserters.fromValue(refreshToken))//본문 내용에 토큰 삽입
                .retrieve()
                .bodyToMono(String.class)//모노 사용, 받는 데이터의 타입을 Sring으로 받기
                .block();

        return newToken;
    }

    public static class Config {
    }
}