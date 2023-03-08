package dev.donmit.donmitapi.model.dto.res;

import java.util.Objects;

import lombok.Builder;

@Builder
public record TokenResDto(String grantType, Long accessTokenExpireDate, String accessToken, String refreshToken) {
	public TokenResDto {
		Objects.requireNonNull(grantType);
		Objects.requireNonNull(accessTokenExpireDate);
		Objects.requireNonNull(accessToken);
		Objects.requireNonNull(refreshToken);
	}
}

