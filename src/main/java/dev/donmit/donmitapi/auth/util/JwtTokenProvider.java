package dev.donmit.donmitapi.auth.util;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import dev.donmit.donmitapi.auth.application.CustomUserDetailsService;
import dev.donmit.donmitapi.auth.dto.TokenResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// JWT 토큰 생성, 토큰 복호화 및 정보 추출, 토큰의 유효성 검증의 기능을 구현한 클래스
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {
	
	// 토큰의 암호화/복호화를 위한 secret key
	@Value("${secretKey}")
	private String secretKey;

	// Refresh Token 유효 기간 14일 (ms 단위)
	private final Long REFRESH_TOKEN_VALID_TIME = 14 * 1440 * 60 * 1000L;

	// Access Token 유효 기간 15분
	private final Long ACCESS_TOKEN_VALID_TIME = 15 * 60 * 1000L;

	private final CustomUserDetailsService userDetailsService;

	// 의존성 주입이 완료된 후에 실행되는 메소드, secretKey를 Base64로 인코딩한다.
	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	// JWT 토큰 생성
	// JWT는 .을 기준으로 header, payload, signature 으로 이루어져 있다.
	public TokenResponseDto createToken(String githubLogin, Long githubId) {

		// payload 에는 토큰에 담을 정보가 들어가는데 이때, 정보의 단위를 클레임(claim)이라고 부르며, 클레임은 key-value 의 한 쌍으로 이루어져 있다.
		// 토큰 제목 설정
		Claims claims = Jwts.claims().setSubject(githubLogin);

		claims.put("githubId", githubId);

		Date now = new Date();

		String accessToken = Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();

		String refreshToken = Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();

		return TokenResponseDto.builder()
			.grantType("bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpireDate(ACCESS_TOKEN_VALID_TIME)
			.build();
	}

	// JWT 토큰을 복호화하여 토큰에 들어있는 사용자 인증 정보 조회
	public Authentication getAuthentication(String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getGithubLogin(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", new ArrayList<>());
	}

	private String getGithubLogin(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody().getSubject();
	}

	// Request의 Header로부터 토큰 값 조회
	public String resolveToken(HttpServletRequest request) {
		return request.getHeader("Authorization");
	}

	// 토큰의 유효성 검증
	public boolean validateToken(String jwtToken) {
		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
			return true;
		} catch (SecurityException e) {
			log.info("Invalid JWT signature.");
			throw new JwtException("Invalid JWT signature.");
		} catch (MalformedJwtException e) {
			log.info("Invalid JWT token.");
			throw new JwtException("Invalid JWT token.");
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT token.");
			throw new JwtException("Expired JWT token.");
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT token.");
		} catch (IllegalArgumentException e) {
			log.info("JWT token compact of handler are invalid.");
			throw new JwtException("JWT token compact of handler are invalid.");
		}
		return false;
	}
}
