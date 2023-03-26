package dev.donmit.donmitapi.auth.dto;

import java.util.Objects;

public record OauthTokenResponseDto(String access_token, String scope, String token_type) {
	
	public OauthTokenResponseDto {
		Objects.requireNonNull(access_token);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(token_type);
	}
}
