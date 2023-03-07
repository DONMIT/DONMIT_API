package dev.donmit.donmitapi.service.auth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.donmit.donmitapi.model.dto.DefaultResponse;
import dev.donmit.donmitapi.model.dto.res.OauthToken;
import dev.donmit.donmitapi.model.dto.res.TokenResDto;
import dev.donmit.donmitapi.model.entity.User;
import dev.donmit.donmitapi.model.util.GithubProfile;
import dev.donmit.donmitapi.model.util.JwtTokenProvider;
import dev.donmit.donmitapi.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OauthService {
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// GitHub에서 넘겨받은 임시 코드로부터 GitHub Access Token을 발급받는 메소드
	public OauthToken getAccessToken(String code, String clientId, String clientSecret) {
		RestTemplate rt = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "application/json");

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("code", code);

		HttpEntity<MultiValueMap<String, String>> githubTokenReq = new HttpEntity<>(params, headers);

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
			log.error("error log = {}", e.getMessage());
		}

		return oauthToken;
	}

	@Transactional(rollbackOn = JsonProcessingException.class)
	public DefaultResponse saveGithubUser(String token) {
		GithubProfile profile = findProfile(token);
		User user = userRepository.findByGithubId(profile.getId());

		if (user == null) {
			user = User.setGithubProfile(profile);
			userRepository.save(user);
		}

		// Access Token과 Refresh Token 새로 발급
		TokenResDto tokenResDto = jwtTokenProvider.createToken(user.getGithubLogin(), user.getGithubId());
		// Refresh Token 저장
		user.saveRefreshToken(tokenResDto.refreshToken());

		return DefaultResponse.response(HttpStatus.OK.value(), "로그인 성공",
			tokenResDto);
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
			log.error("error log = {}", e.getMessage());
		}

		return githubProfile;
	}
}
