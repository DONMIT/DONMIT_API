package com.example.donmit.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubProfile {

	// start: 회원가입시 저장하는 내용

	public String login; // github 아이디

	public Long id; // github 계정 고유 식별자

	@JsonProperty("avatar_url")
	public String avatarUrl; // github 계정 프로필 썸네일

	public String name; // github 이름

	public String blog;

	public String email;

	// end

	@JsonProperty("node_id")
	public String nodeId;

	@JsonProperty("gravatar_id")
	public String gravatarId;
	public String url;

	@JsonProperty("html_url")
	public String htmlUrl;

	@JsonProperty("followers_url")
	public String followersUrl;

	@JsonProperty("following_url")
	public String followingUrl;

	@JsonProperty("gists_url")
	public String gistsUrl;

	@JsonProperty("starred_url")
	public String starredUrl;

	@JsonProperty("subscriptions_url")
	private String subscriptionsUrl;

	@JsonProperty("organizations_url")
	private String organizationsUrl;

	@JsonProperty("repos_url")
	private String reposUrl;

	@JsonProperty("events_url")
	private String eventsUrl;

	@JsonProperty("received_events_url")
	private String receivedEventsUrl;
	private String type;

	@JsonProperty("site_admin")
	private boolean siteAdmin;

	private String company;
	private String location;
	public boolean hireable;
	public String bio;

	@JsonProperty("twitter_username")
	public String twitterUsername;

	@JsonProperty("public_repos")
	public int publicRepos;

	@JsonProperty("public_gists")
	public int publicGists;

	public int followers;
	public int following;

	@JsonProperty("created_at")
	public String createdAt;

	@JsonProperty("updated_at")
	public String updatedAt;

	@JsonProperty("private_gists")
	public int privateGists;

	@JsonProperty("total_private_repos")
	public int totalPrivateRepos;

	@JsonProperty("owned_private_repos")
	public int ownedPrivateRepos;

	@JsonProperty("disk_usage")
	public int diskUsage;

	public int collaborators;

	@JsonProperty("two_factor_authentication")
	public boolean twoFactorAuthentication;
	public Plan plan;

	@Getter
	@Setter
	private static class Plan {
		public String name;
		public int space;
		public int collaborators;

		@JsonProperty("private_repos")
		public int privateRepos;
	}
}
