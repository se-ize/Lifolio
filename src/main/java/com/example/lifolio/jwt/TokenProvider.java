package com.example.lifolio.jwt;

import com.example.lifolio.base.BaseException;
import com.example.lifolio.entity.User;
import com.example.lifolio.repository.UserRepository;
import com.example.lifolio.service.CustomUserDetailsService;
import com.example.lifolio.service.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static com.example.lifolio.base.BaseResponseStatus.*;
import static com.example.lifolio.jwt.JwtFilter.AUTHORIZATION_HEADER;

@Component
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private CustomUserDetailsService customUserDetailsService;

    private final UserRepository userRepository;


    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final String refreshSecret;
    private final RedisService redisService;
    private final long accessTime;
    private final long refreshTime;
    private Key key;




    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.refresh}") String refreshSecret,
                    UserRepository userRepository,
            CustomUserDetailsService customUserDetailsService,
            RedisService redisService, @Value("${jwt.access-token-seconds}") long accessTime, @Value("${jwt.refresh-token-seconds}")long refreshTime) {
        this.secret = secret;
        this.userRepository = userRepository;
        this.refreshSecret=refreshSecret;
        this.customUserDetailsService=customUserDetailsService;
        this.redisService = redisService;
        this.accessTime = accessTime*1000;
        this.refreshTime = refreshTime*1000;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long userId) {
        Date now =new Date();

        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+accessTime))
                .signWith(SignatureAlgorithm.HS256,secret)
                .compact();
    }

    public String createRefreshToken(Long userId){
        Date now=new Date();

        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+refreshTime))
                .signWith(SignatureAlgorithm.HS256,refreshSecret)
                .compact();

    }

    public Authentication getAuthentication(String token)  {
        Jws<Claims> claims;

        claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token);


        Long userId=claims.getBody().get("userId",Long.class);

        Optional<User> users=userRepository.findById(userId);
        String userName = users.get().getUsername();

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);

        return new UsernamePasswordAuthenticationToken(users.get(),"",userDetails.getAuthorities());
    }


    public boolean validateToken(ServletRequest servletRequest, String token) {
        try {
            Jws<Claims> claims;
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);

            Long userId = claims.getBody().get("userId",Long.class);

            String expiredAt= redisService.getValues(token);


            if(expiredAt==null){
                return true;
            }
            if(expiredAt.equals(String.valueOf(userId))){
                servletRequest.setAttribute("exception","HijackException");
                return false;
            }


            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            servletRequest.setAttribute("exception","MalformedJwtException");
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            servletRequest.setAttribute("exception","ExpiredJwtException");
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            servletRequest.setAttribute("exception","UnsupportedJwtException");
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            servletRequest.setAttribute("exception","IllegalArgumentException");
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(AUTHORIZATION_HEADER);
    }


    public void logOut(Long userId, String accessToken) {
        long expiredAccessTokenTime=getExpiredTime(accessToken).getTime() - new Date().getTime();
        //Redis 에 액세스 토큰값을 key 로 가지는 userId 값 저장
        redisService.saveToken(accessToken,String.valueOf(userId),expiredAccessTokenTime);
        //Redis 에 저장된 refreshToken 삭제
        redisService.deleteValues(String.valueOf(userId));
    }

    public Date getExpiredTime(String token){
        //받은 토큰의 유효 시간을 받아오기
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration();
    }
}