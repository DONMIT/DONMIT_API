package dev.donmit.donmitapi.global.config.security;

import static dev.donmit.donmitapi.global.common.Constants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		final Map<String, Object> body = new HashMap<>();
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		// 응답 객체 초기화
		body.put(STATUS, HttpServletResponse.SC_UNAUTHORIZED);
		body.put(ERROR, UNAUTHORIZED);
		body.put(MESSAGE, authException.getMessage());
		body.put(PATH, request.getServletPath());

		final ObjectMapper mapper = new ObjectMapper();

		// response 객체에 응답 객체를 넣어줌
		mapper.writeValue(response.getOutputStream(), body);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
