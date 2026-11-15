package com.astrahealth.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefreshTokenRequest {

	@NotBlank(message = "Refresh token is required.")
	@Size(max = 512, message = "Refresh token is invalid.")
	private String refreshToken;

	public RefreshTokenRequest() {
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
