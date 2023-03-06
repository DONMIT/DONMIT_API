package dev.donmit.donmitapi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.donmit.donmitapi.model.entity.User;

@RestController
public class TestController {
	@GetMapping("/hello")
	public String test(@AuthenticationPrincipal User user) {
		return user.getUsername() + "님 환영합니다.";
	}
}
