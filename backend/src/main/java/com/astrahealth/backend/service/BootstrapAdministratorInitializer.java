package com.astrahealth.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Component
@Profile("!test")
public class BootstrapAdministratorInitializer implements ApplicationRunner {

	private final AuthenticationService authenticationService;
	private final String email;
	private final String password;

	public BootstrapAdministratorInitializer(
			AuthenticationService authenticationService,
			@Value("${ASTRA_BOOTSTRAP_ADMIN_EMAIL:}") String email,
			@Value("${ASTRA_BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
		this.authenticationService = authenticationService;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments arguments) {
		boolean hasEmail = StringUtils.hasText(email);
		boolean hasPassword = StringUtils.hasText(password);
		if (!hasEmail && !hasPassword) {
			return;
		}
		if (!hasEmail || !hasPassword) {
			throw new IllegalStateException(
					"Both ASTRA_BOOTSTRAP_ADMIN_EMAIL and ASTRA_BOOTSTRAP_ADMIN_PASSWORD must be configured together.");
		}

		authenticationService.provisionBootstrapAdministrator(email, password);
	}
}
