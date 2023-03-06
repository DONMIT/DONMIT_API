package dev.donmit.donmitapi.model.dto.res;

import lombok.Builder;

@Builder
public record TokenResDto(String grantType, Long accessTokenExpireDate, String accessToken, String refreshToken) {
}

