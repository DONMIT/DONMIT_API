package com.example.donmit.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DefaultRes<T> {

	// API 상태 코드
	private Integer statusCode;

	// API 부가 설명
	private String message;

	// API 응답 데이터
	private T data;

	// 상태 코드 + 부가 설명 반환
	public static <T> DefaultRes<T> response(final Integer statusCode, final String message) {
		return (DefaultRes<T>)DefaultRes.builder()
			.statusCode(statusCode)
			.message(message)
			.build();
	}

	// 상태 코드 + 부가 설명 + 응답 데이터 반환
	public static <T> DefaultRes<T> response(final Integer statusCode, final String message, final T data) {
		return (DefaultRes<T>)DefaultRes.builder()
			.statusCode(statusCode)
			.message(message)
			.data(data)
			.build();
	}

}
