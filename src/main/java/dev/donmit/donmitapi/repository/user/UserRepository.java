package dev.donmit.donmitapi.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import dev.donmit.donmitapi.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByGithubId(Long githubId);

	Optional<UserDetails> findByGithubLogin(String username);
}
