package dev.donmit.donmitapi.auth.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.donmit.donmitapi.auth.application.AuthService;
import dev.donmit.donmitapi.auth.dto.OauthTokenResponseDto;
import dev.donmit.donmitapi.global.common.response.DefaultResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthApi {

	private final AuthService authService;

	@Value("${client_id}")
	private String clientId;

	@Value("${client_secret}")
	private String clientSecret;

	@GetMapping("/login/oauth2/code/github")
	public ResponseEntity<DefaultResponse> gitHubOauth(@RequestParam String code) {
		// 발급된 임시 코드를 통해 GitHub access Token 발급
		OauthTokenResponseDto oauthTokenResponseDto = authService.getAccessToken(code, clientId,
			clientSecret);

		if (oauthTokenResponseDto != null) {
			// 발급 받은 Github accessToken 으로 GitHub에 저장되어 있는 회원 정보를 불러와, DB에 저장한 후 우리 서비스의 Access Token, Refresh Token 발급
			DefaultResponse res = authService.saveGithubUser(oauthTokenResponseDto.access_token());
			return new ResponseEntity<>(res, HttpStatus.OK);
		}

		return new ResponseEntity<>(
			DefaultResponse.response(HttpStatus.OK.value(), "로그인 실패 ( => GitHub Access Token 발급 실패)"),
			HttpStatus.OK);
	}
}
