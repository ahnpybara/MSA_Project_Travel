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

    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
    
    private WebClient webClient;

    public AuthorizationFilter() {
        super(Config.class);
        this.webClient = WebClient.create("http://35.239.40.37:8080");
    }

    //필터 수행 메서드
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.error("로그인 상태 및 토큰을 다시 확인할 것");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                        .wrap("로그인이 필요합니다.".getBytes(StandardCharsets.UTF_8))));
            }
            String token = authorizationHeader.substring(JwtProperties.TOKEN_PREFIX.length());

            try {
                verifyToken(token);
                return chain.filter(exchange);
            } catch (TokenExpiredException e) {
                String username;
                try {
                    DecodedJWT decodedJWT = JWT.decode(token);
                    Claim usernameClaim = decodedJWT.getClaim("username");
                    if (usernameClaim.isNull()) {
                        throw new JWTVerificationException("username claim이 없습니다.");
                    }
                    username = usernameClaim.asString();
                } catch (Exception ex) {
                    logger.error("토큰 디코딩 실패", ex);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap("토큰 디코딩에 실패했습니다.".getBytes(StandardCharsets.UTF_8))));
                }

                try {
                    return getRefreshToken(username)
                            .flatMap(this::requestNewToken)
                            .flatMap(newToken -> {
                                exchange.getResponse().getHeaders().set("authorization", "Bearer " + newToken);
                                return chain.filter(exchange);
                            });
                } catch (Exception ex) {
                    logger.error("토큰 재발급 실패", ex);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap("토큰 재발급에 실패했습니다.".getBytes(StandardCharsets.UTF_8))));
                }
            } catch (Exception e) {
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
        logger.info("사용자 조회 요청");
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
    public static class Config {}
}
