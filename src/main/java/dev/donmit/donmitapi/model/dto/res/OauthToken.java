package dev.donmit.donmitapi.model.dto.res;

import java.util.Objects;

public record OauthToken(String access_token, String scope, String token_type) {
	public OauthToken {
		Objects.requireNonNull(access_token);
		Objects.requireNonNull(scope);
		Objects.requireNonNull(token_type);
	}
}
