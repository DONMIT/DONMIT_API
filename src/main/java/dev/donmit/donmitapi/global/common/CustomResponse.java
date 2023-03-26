package dev.donmit.donmitapi.global.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomResponse {

	LOGIN_SUCCESS(HttpStatus.OK.value(), "로그인 성공"),
	LOGIN_FAILURE(HttpStatus.OK.value(), "로그인 실패 (원인: => GitHub Access Token 발급 실패)");

	private final int status;
	private final String message;

}
