package com.example.donmit.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.donmit.model.dto.DefaultRes;
import com.example.donmit.model.dto.res.OauthToken;
import com.example.donmit.service.auth.OauthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final OauthService oauthService;

	@GetMapping("/login/oauth2/code/github")
	public ResponseEntity<DefaultRes> gitHubOauth(@RequestParam String code) {

		// 발급된 임시 코드를 통해 GitHub access Token 발급
		OauthToken oauthToken = oauthService.getAccessToken(code);

		// 발급 받은 Github accessToken 으로 Github에 저장되어 있는 회원 정보를 불러와, DB에 저장한 후 우리 서비스의 Access Token, Refresh Token 발급
		DefaultRes res = oauthService.saveGithubUser(oauthToken.getAccess_token());

		return new ResponseEntity<>(res, HttpStatus.OK);

	}
}
