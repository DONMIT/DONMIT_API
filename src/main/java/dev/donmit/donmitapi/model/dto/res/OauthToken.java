package dev.donmit.donmitapi.model.dto.res;

public record OauthToken(String access_token, String scope, String token_type) {
}
