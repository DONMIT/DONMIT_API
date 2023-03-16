package dev.donmit.donmitapi.auth.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.donmit.donmitapi.auth.dao.UserRepository;
import dev.donmit.donmitapi.auth.domain.User;
import dev.donmit.donmitapi.auth.dto.GithubProfileRequestDto;
import dev.donmit.donmitapi.auth.dto.OauthTokenResponseDto;
import dev.donmit.donmitapi.auth.dto.TokenResponseDto;
import dev.donmit.donmitapi.auth.util.JwtTokenProvider;
import dev.donmit.donmitapi.global.common.response.DefaultResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final WebClient gitHubWebClient;

	// GitHub에서 넘겨받은 임시 코드로부터 GitHub Access Token을 발급받는 메소드
	public OauthTokenResponseDto getAccessToken(String code, String clientId,
		String clientSecret) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("code", code);

		return gitHubWebClient.mutate()
			.baseUrl("https://github.com")
			.build()
			.post()
			.uri("/login/oauth/access_token")
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(params)
			.retrieve()
			.bodyToMono(OauthTokenResponseDto.class)
			.block();
	}

	@Transactional(rollbackOn = JsonProcessingException.class)
	public DefaultResponse saveGithubUser(String token) {
		GithubProfileRequestDto profile = findProfile(token);
		User user = userRepository.findByGithubId(profile.getId());

		if (user == null) {
			user = User.setGithubProfile(profile);
			userRepository.save(user);
		}

		// Access Token과 Refresh Token 새로 발급
		TokenResponseDto tokenResponseDto = jwtTokenProvider.createToken(user.getGithubLogin(), user.getGithubId());
		// Refresh Token 저장
		user.saveRefreshToken(tokenResponseDto.refreshToken());

		return DefaultResponse.response(HttpStatus.OK.value(), "로그인 성공",
			tokenResponseDto);
	}

	public GithubProfileRequestDto findProfile(String token) {
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
		GithubProfileRequestDto githubProfileRequestDto = null;
		try {
			githubProfileRequestDto = objectMapper.readValue(githubProfileResponse.getBody(),
				GithubProfileRequestDto.class);
		} catch (JsonProcessingException e) {
			log.error("error log = {}", e.getMessage());
		}

		return githubProfileRequestDto;
	}
}
