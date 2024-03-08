package travel.auth;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import reactor.core.publisher.Mono;

@Component
// 토큰을 검증하고 필요한 경우 재발급하는 필터
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    // 로그를 위한 Logger 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    // HTTP 요청을 보내기 위한 WebClient 인스턴스
    private WebClient webClient;

    // 생성자에서 WebClient 인스턴스를 초기화
    public AuthorizationFilter() {
        super(Config.class);
        this.webClient = WebClient.create("http://localhost:8088");
    }

    // GatewayFilter를 반환하는 메소드
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 요청 헤더에서 "Authorization" 헤더를 가져옴
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            // "Authorization" 헤더가 없거나 "Bearer "로 시작하지 않으면 로그를 남기고 401 Unauthorized 응답을 반환
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.error("로그인 상태 및 토큰을 다시 확인할 것");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                // 로그인이 필요하다는 응답 메시지 추가
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                        .wrap("로그인이 필요합니다.".getBytes(StandardCharsets.UTF_8))));
            }
            String token = authorizationHeader.substring(JwtProperties.TOKEN_PREFIX.length());

            try {
                // 토큰을 검증
                verifyToken(token);
                return chain.filter(exchange);
            } catch (TokenExpiredException e) {
                // 토큰이 만료된 경우
                String username;
                try {
                    // 토큰에서 "username" 클레임을 추출
                    DecodedJWT decodedJWT = JWT.decode(token);
                    Claim usernameClaim = decodedJWT.getClaim("username");
                    if (usernameClaim.isNull()) {
                        throw new JWTVerificationException("username claim이 없습니다.");
                    }
                    username = usernameClaim.asString();
                } catch (Exception ex) {
                    // 토큰 디코딩에 실패한 경우 로그를 남기고 401 Unauthorized 응답을 반환
                    logger.error("토큰 디코딩 실패", ex);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap("토큰 디코딩에 실패했습니다.".getBytes(StandardCharsets.UTF_8))));
                }

                try {
                    // 리프레시 토큰을 가져와서 새 토큰을 발급받음
                    return getRefreshToken(username)
                            .flatMap(this::requestNewToken)
                            .flatMap(newToken -> {
                                exchange.getResponse().getHeaders().set("authorization", "Bearer " + newToken);
                                return chain.filter(exchange);
                            });
                } catch (Exception ex) {
                    // 토큰 재발급에 실패한 경우 로그를 남기고 500 Internal Server Error 응답을 반환
                    logger.error("토큰 재발급 실패", ex);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap("토큰 재발급에 실패했습니다.".getBytes(StandardCharsets.UTF_8))));
                }
            } catch (Exception e) {
                // 토큰 검증에 실패한 경우 로그를 남기고 401 Unauthorized 응답을 반환
                logger.error("토큰 검증 실패", e);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                        .wrap("토큰 검증에 실패했습니다.".getBytes(StandardCharsets.UTF_8))));
            }
        };
    }

    // 토큰을 검증하는 메소드
    private void verifyToken(String token) throws Exception {
        try {
            JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()))
                    .build()
                    .verify(token) 
                    .getClaims();
            logger.info("토큰 유효함");
        } catch (TokenExpiredException e) {
            logger.error("토큰이 만료됨", e);
            throw e;
        } catch (JWTVerificationException e) {
            logger.error("토큰 검증 실패", e);
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    // 사용자 이름으로 리프레시 토큰을 가져오는 메소드
    private Mono<String> getRefreshToken(String username) {
        return webClient.get()
                .uri("/users/{username}/refreshToken", username)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    return Mono.error(new RuntimeException("리프레쉬 토큰을 가져오는데 실패하였습니다."));
                })
                .bodyToMono(String.class);
    }
    
    // 리프레시 토큰으로 새 토큰을 발급받는 메소드
    private Mono<String> requestNewToken(String refreshToken) {
        logger.info("토큰 재발급 요청");
        return webClient.post()
                .uri("/users/token/refresh")
                .body(BodyInserters.fromValue(refreshToken))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    return Mono.error(new RuntimeException("토큰 재발급 요청에 실패하였습니다."));
                })
                .bodyToMono(String.class);
    }

    // 필터 설정을 위한 빈 클래스
    public static class Config {
    }
}
