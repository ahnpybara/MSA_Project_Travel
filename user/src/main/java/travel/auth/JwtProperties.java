package travel.auth;

public interface JwtProperties {
    String TOKEN_PREFIX = "Bearer ";//접두, 공백을 만들면 헤더에 저장할 수 있고 공백이 없으면 쿠키 
    String HEADER_STRING = "Authorization";
    int AJ_TIME=10;// jwt
    int RT_TIME=10080;
    String TOKENNAME = "COS토큰";
    String SECRET = "mcos"; //비밀값


}