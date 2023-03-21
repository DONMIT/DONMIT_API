package dev.donmit.donmitapi.global.config.security;

import static dev.donmit.donmitapi.global.config.security.JwtExceptionFilter.*;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.donmit.donmitapi.auth.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * 클라이언트 요청 시 JWT 인증을 위한 커스텀 필터로,
 * UsernamePasswordAuthenticationFilter 이전에 실행되는 필터
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return skipFilterUrls.stream().anyMatch(url -> new AntPathRequestMatcher(url).matches(request));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		// 헤더로부터 JWT를 받아온다.
		String token = jwtTokenProvider.resolveToken((HttpServletRequest)request);

		// 유효한 토큰인지 확인한다.
		if (token != null && jwtTokenProvider.validateToken(token)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			// SecurityContext에 Authentication 객체를 저장한다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}
}
