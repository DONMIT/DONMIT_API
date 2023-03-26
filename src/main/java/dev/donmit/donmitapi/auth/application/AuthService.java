package dev.donmit.donmitapi.auth.application;

import static dev.donmit.donmitapi.global.common.Constants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.donmit.donmitapi.auth.dao.UserRepository;
import dev.donmit.donmitapi.auth.domain.User;
import dev.donmit.donmitapi.auth.dto.GithubProfileRequestDto;
import dev.donmit.donmitapi.auth.dto.OauthTokenResponseDto;
import dev.donmit.donmitapi.auth.dto.TokenResponseDto;
import dev.donmit.donmitapi.auth.util.JwtTokenProvider;
import dev.donmit.donmitapi.global.common.CustomResponse;
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

	@Value("${client_id}")
	private String clientId;

	@Value("${client_secret}")
	private String clientSecret;

	@Value("${gitHub.base_url.access_code}")
	private String accessCodeBaseUrl;

	@Value("${gitHub.base_url.profile}")
	private String gitHubProfileBaseUrl;

	// GitHub에서 넘겨받은 임시 코드로부터 GitHub Access Token을 발급받는 메소드
	public OauthTokenResponseDto getAccessToken(String code) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(CLIENT_ID, clientId);
		params.add(CLIENT_SECRET, clientSecret);
		params.add(CODE, code);

		return gitHubWebClient.mutate()
			.baseUrl(accessCodeBaseUrl)
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

		return DefaultResponse.response(CustomResponse.LOGIN_SUCCESS.getStatus(),
			CustomResponse.LOGIN_SUCCESS.getMessage(),
			tokenResponseDto);
	}

	public GithubProfileRequestDto findProfile(String token) {
		return gitHubWebClient.mutate()
			.baseUrl(gitHubProfileBaseUrl)
			.build()
			.post()
			.uri("/user")
			.header(AUTHORIZATION, BEARER_SPACE + token)
			.retrieve()
			.bodyToMono(GithubProfileRequestDto.class)
			.block();
	}
}
