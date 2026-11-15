package com.astrahealth.backend.dto.response;

public class AuthTokenResponse {

	private final String accessToken;
	private final String tokenType;
	private final long expiresIn;
	private final String refreshToken;

	public AuthTokenResponse(String accessToken, long expiresIn, String refreshToken) {
		this.accessToken = accessToken;
		this.tokenType = "Bearer";
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
}
