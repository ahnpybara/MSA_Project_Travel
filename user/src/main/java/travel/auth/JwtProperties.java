package travel.auth;

public interface JwtProperties {
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
    int AJ_TIME=10;
    int RT_TIME=10080;
    String TOKENNAME = "COS토큰";
    String SECRET = "travelProjectCode"; 
}