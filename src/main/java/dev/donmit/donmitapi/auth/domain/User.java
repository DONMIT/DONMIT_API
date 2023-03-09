package dev.donmit.donmitapi.auth.domain;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.donmit.donmitapi.auth.dto.GithubProfileRequestDto;
import dev.donmit.donmitapi.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	// GitHub 계정 아이디
	private String githubLogin;

	// GitHub 계정 식별자
	private Long githubId;

	// GitHub 계정 이름
	private String githubName;
	private String thumbnail;
	private String email;
	private String blog;
	private String refreshToken;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return githubLogin;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Builder
	public User(Long githubId, String githubName, String email, String blog, String githubLogin, String thumbnail) {
		this.githubId = githubId;
		this.githubName = githubName;
		this.email = email;
		this.blog = blog;
		this.githubLogin = githubLogin;
		this.thumbnail = thumbnail;
	}

	public static User setGithubProfile(GithubProfileRequestDto profile) {
		return User.builder()
			.githubId(profile.getId())
			.githubLogin(profile.getLogin())
			.githubName(profile.getName())
			.blog(profile.getBlog())
			.email(profile.getEmail())
			.thumbnail(profile.getAvatarUrl())
			.build();
	}

	public void saveRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
