package dev.donmit.donmitapi.auth.dto;

import java.util.Objects;

import lombok.Builder;

@Builder
public record TokenResponseDto(String grantType, Long accessTokenExpireDate, String accessToken, String refreshToken) {
	public TokenResponseDto {
		Objects.requireNonNull(grantType);
		Objects.requireNonNull(accessTokenExpireDate);
		Objects.requireNonNull(accessToken);
		Objects.requireNonNull(refreshToken);
	}
}

