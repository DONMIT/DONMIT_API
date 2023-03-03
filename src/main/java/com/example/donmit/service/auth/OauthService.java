package com.example.donmit.service.auth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.donmit.model.dto.DefaultRes;
import com.example.donmit.model.dto.res.OauthToken;
import com.example.donmit.model.dto.res.TokenResDto;
import com.example.donmit.model.entity.User;
import com.example.donmit.model.util.GithubProfile;
import com.example.donmit.model.util.JwtTokenProvider;
import com.example.donmit.repository.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OauthService {
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// GitHub에서 넘겨받은 임시 코드로부터, GitHub Access Token을 발급받는 메소드
	public OauthToken getAccessToken(String code) {

		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "application/json");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", "a5bfcce3965274299f58");
		params.add("client_secret", "f1b5a8135ca277782438a4c1a5cf562371770cfc");
		params.add("code", code);

		HttpEntity<MultiValueMap<String, String>> githubTokenReq =
			new HttpEntity<>(params, headers);

		ResponseEntity<String> accessTokenResponse = rt.exchange(
			"https://github.com/login/oauth/access_token",
			HttpMethod.POST,
			githubTokenReq,
			String.class
		);

		ObjectMapper objectMapper = new ObjectMapper();
		OauthToken oauthToken = null;

		try {
			oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return oauthToken;
	}

	@Transactional
	public DefaultRes saveGithubUser(String token) {
		GithubProfile profile = findProfile(token);
		User user = userRepository.findByGithubId(profile.getId());

		if (user == null) {
			user = User.builder()
				.githubId(profile.getId())
				.githubLogin(profile.getLogin())
				.githubName(profile.getName())
				.blog(profile.getBlog())
				.email(profile.getEmail())
				.thumbnail(profile.getAvatarUrl())
				.build();

			userRepository.save(user);
		}

		// Access Token과 Refresh Token 새로 발급
		TokenResDto tokenResDto = jwtTokenProvider.createToken(user.getGithubLogin(), user.getGithubId());

		// Refresh Token 저장
		user.saveRefreshToken(tokenResDto.getRefreshToken());

		return DefaultRes.response(HttpStatus.OK.value(), "로그인에 성공하였습니다.", tokenResDto);
	}

	public GithubProfile findProfile(String token) {
		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);

		HttpEntity<MultiValueMap<String, String>> githubProfileRequest =
			new HttpEntity<>(headers);

		ResponseEntity<String> githubProfileResponse = rt.exchange(
			"https://api.github.com/user",
			HttpMethod.POST,
			githubProfileRequest,
			String.class
		);

		ObjectMapper objectMapper = new ObjectMapper();
		GithubProfile githubProfile = null;
		try {
			githubProfile = objectMapper.readValue(githubProfileResponse.getBody(), GithubProfile.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return githubProfile;
	}
}
