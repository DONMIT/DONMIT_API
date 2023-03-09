package dev.donmit.donmitapi.test.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.donmit.donmitapi.auth.domain.User;

@RestController
public class TestApi {
	@GetMapping("/hello")
	public String test(@AuthenticationPrincipal User user) {
		return user.getUsername() + "님 환영합니다.";
	}
}
