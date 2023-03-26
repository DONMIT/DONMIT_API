package dev.donmit.donmitapi.auth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.donmit.donmitapi.auth.application.AuthService;
import dev.donmit.donmitapi.auth.dto.OauthTokenResponseDto;
import dev.donmit.donmitapi.global.common.CustomResponse;
import dev.donmit.donmitapi.global.common.response.DefaultResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthApi {

	private final AuthService authService;

	@GetMapping("/login/oauth2/code/github")
	public DefaultResponse gitHubOauth(@RequestParam String code) {
		// 발급된 임시 코드를 통해 GitHub access Token 발급
		OauthTokenResponseDto oauthTokenResponseDto = authService.getAccessToken(code);

		if (oauthTokenResponseDto != null) {
			// 발급 받은 Github accessToken 으로 GitHub에 저장되어 있는 회원 정보를 불러와, DB에 저장한 후 우리 서비스의 Access Token, Refresh Token 발급
			DefaultResponse response = authService.saveGithubUser(oauthTokenResponseDto.access_token());
			return response;
		}

		return DefaultResponse.response(CustomResponse.LOGIN_FAILURE.getStatus(),
			CustomResponse.LOGIN_FAILURE.getMessage());
	}
}
