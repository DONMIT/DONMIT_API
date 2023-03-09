package dev.donmit.donmitapi.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.donmit.donmitapi.auth.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] AUTH_WHITELIST = {
		"/", "/login/oauth2/**"
	};

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtExceptionFilter jwtExceptionFilter;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
		Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	protected SecurityFilterChain config(HttpSecurity http) throws Exception {
		return http
			.csrf().disable()
			.httpBasic().disable()
			.formLogin().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(AUTH_WHITELIST).permitAll()
				.anyRequest().authenticated()
			)

			.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
			.and()

			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class)
			.build();
	}
}
