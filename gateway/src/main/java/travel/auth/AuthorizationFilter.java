package travel.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private WebClient webClient;

    public AuthorizationFilter() {

        super(Config.class);
        this.webClient = WebClient.create("http://34.69.178.156:8080");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                System.out.println("============================================================================ 토큰을 다시 확인할 것 ");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authorizationHeader.substring(7);

            if (isTokenExpired(token)) {

                DecodedJWT decodedJWT = JWT.decode(token); 
                String username = decodedJWT.getClaim("username").asString();


                return getRefreshToken(username)
                    .flatMap(this::requestNewToken)
                    .flatMap(newToken -> {
                        exchange.getResponse().getHeaders().set("authorization", "Bearer " + newToken);
                        return chain.filter(exchange);
                    });
            }
            return chain.filter(exchange);
        };
    }

    private boolean isTokenExpired(String token) {
        try {
            JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()))
                    .build()
                    .verify(token)// 검증
                    .getClaims();// 검증된 토큰의 claims를 가져옴
            System.out.println("토큰이 유효함 ");
            return false;
        } catch (TokenExpiredException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    // 리프레쉬 토큰 알아내기 위한 것
    private Mono<String> getRefreshToken(String username) {
        return webClient.get()
                .uri("/users/{username}/refreshToken", username)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<String> requestNewToken(String refreshToken) {
        System.out.println("재발급 요청");
        return webClient.post()
                .uri("/users/token/refresh")
                .body(BodyInserters.fromValue(refreshToken))
                .retrieve()
                .bodyToMono(String.class);
    }
    // 리액티브 프로그래밍 라이브러리의 핵심 타입 중 하나입니다
    //Mono가 가지고 있는 '데이터 단일성' 때문입니다. 
    //Mono는 0개 또는 1개의 결과만을 반환하는 스트림이라는 특징을
    // 가지고 있습니다. 
    //Mono를 사용하면, 결과가 필요한 시점에만 데이터를 처리하게 됩니다. 
    //이렇게 되면 자원을 효율적으로 사용할 수 있게 됩니다.
    // Mono는 비동기적인 작업의 결과를 나타낼 때 주로 사용됩니다.
    // 예를 들어, 파일에서 데이터를 읽거나 원격 서비스에서 데이터를 가져오는 등의 작업에서 Mono를 사용할 수 있습니다.




//     효율성: 비동기 프로그래밍을 사용하면, 오래 걸리는 작업을 기다리는 동안 다른 작업을 수행할 수 있습니다. 이로 인해 프로그램의 효율성을 높일 수 있습니다.
// 응답성: 비동기 프로그래밍을 사용하면, 오래 걸리는 작업을 처리하는 동안도 사용자 인터페이스와 같은 것을 계속해서 업데이트할 수 있습니다. 이로 인해 프로그램의 응답성을 높일 수 있습니다.
// 병렬성: 비동기 프로그래밍을 사용하면, 여러 작업을 동시에 병렬로 처리할 수 있습니다. 이로 인해 프로그램의 성능을 높일 수 있습니다.

    public static class Config {
    }
}