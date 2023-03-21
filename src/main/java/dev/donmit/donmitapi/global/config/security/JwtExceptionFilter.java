package dev.donmit.donmitapi.global.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

	static List<String> skipFilterUrls = Arrays.asList("/", "/login/oauth2/**");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return skipFilterUrls.stream().anyMatch(url -> new AntPathRequestMatcher(url).matches(request));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		ServletException, IOException {
		try {
			chain.doFilter(request, response); // JwtAuthenticationFilter로 이동
		} catch (JwtException e) {
			// JwtAuthenticationFilter에서 예외 발생하면 바로 setErrorResponse 호출
			log.error(e.getMessage());
			setErrorResponse(request, response, e);
		}
	}

	public void setErrorResponse(HttpServletRequest req, HttpServletResponse res, Throwable cause) throws IOException {
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);

		final Map<String, Object> body = new HashMap<>();

		body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		body.put("error", "Unauthorized");
		body.put("message", cause.getMessage());
		body.put("path", req.getServletPath());

		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(res.getOutputStream(), body);
		res.setStatus(HttpServletResponse.SC_OK);
	}
}
