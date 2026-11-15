package com.astrahealth.backend.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI astraHealthOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("ASTRA HEALTH API")
						.version("1.0.0")
						.description("Astronaut health monitoring, identity, operations, and well-being APIs. "
								+ "Health rules are software workflow signals and are not medical diagnoses."))
				.components(new Components().addSecuritySchemes(
						"bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}

	@Bean
	OpenApiCustomizer protectAuthenticatedOperationsInDocs() {
		return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
			if (isPublicPath(path)) {
				return;
			}
			pathItem.readOperations().forEach(operation ->
					operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth")));
		});
	}

	private boolean isPublicPath(String path) {
		return "/api/v1/health".equals(path)
				|| "/api/v1/auth/register".equals(path)
				|| "/api/v1/auth/login".equals(path)
				|| "/api/v1/auth/refresh".equals(path);
	}
}
