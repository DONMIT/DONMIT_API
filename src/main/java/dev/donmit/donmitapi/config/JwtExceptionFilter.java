package dev.donmit.donmitapi.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
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
