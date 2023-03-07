package dev.donmit.donmitapi.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.donmit.donmitapi.model.dto.DefaultResponse;
import dev.donmit.donmitapi.model.dto.res.OauthToken;
import dev.donmit.donmitapi.service.auth.OauthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final OauthService oauthService;
	@Value("${client_id}")
	private String clientId;
	@Value("${client_secret}")
	private String clientSecret;

	@GetMapping("/login/oauth2/code/github")
	public ResponseEntity<DefaultResponse> gitHubOauth(@RequestParam String code) {
		// 발급된 임시 코드를 통해 GitHub access Token 발급
		OauthToken oauthToken = oauthService.getAccessToken(code, clientId, clientSecret);

		if (oauthToken != null) {
			// 발급 받은 Github accessToken 으로 GitHub에 저장되어 있는 회원 정보를 불러와, DB에 저장한 후 우리 서비스의 Access Token, Refresh Token 발급
			DefaultResponse res = oauthService.saveGithubUser(oauthToken.access_token());
			return new ResponseEntity<>(res, HttpStatus.OK);
		}

		return new ResponseEntity<>(
			DefaultResponse.response(HttpStatus.OK.value(), "로그인 실패 ( => GitHub Access Token 발급 실패)"),
			HttpStatus.OK);
	}
}
