package com.astrahealth.backend.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.astrahealth.backend.dto.response.ApiErrorResponse;
import com.astrahealth.backend.entity.AccountStatus;
import com.astrahealth.backend.entity.UserAccount;
import com.astrahealth.backend.repository.UserAccountRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

	@Bean
	SecretKey jwtSigningKey(
			@Value("${astra.security.jwt.secret:${ASTRA_JWT_SECRET:}}") String encodedSecret) {
		if (encodedSecret.isBlank()) {
			throw new IllegalStateException("ASTRA_JWT_SECRET must be configured before the application can start.");
		}

		byte[] secretBytes;
		try {
			secretBytes = Base64.getDecoder().decode(encodedSecret);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("ASTRA_JWT_SECRET must be a Base64-encoded key.", exception);
		}
		if (secretBytes.length < 32) {
			throw new IllegalStateException("ASTRA_JWT_SECRET must decode to at least 32 bytes.");
		}

		return new SecretKeySpec(secretBytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
		return NimbusJwtDecoder.withSecretKey(jwtSigningKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	Clock systemClock() {
		return Clock.systemUTC();
	}

	@Bean
	SecureRandom secureRandom() {
		return new SecureRandom();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${astra.security.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		configuration.setExposedHeaders(List.of("Content-Disposition", "X-Content-Type-Options"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter(UserAccountRepository userAccountRepository) {
		Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter = jwt -> {
			long accountId;
			try {
				accountId = Long.parseLong(jwt.getSubject());
			} catch (NumberFormatException exception) {
				throw new InvalidBearerTokenException("Invalid bearer token.");
			}

			UserAccount account = userAccountRepository.findById(accountId)
					.filter(candidate -> candidate.getStatus() == AccountStatus.ACTIVE)
					.orElseThrow(() -> new InvalidBearerTokenException("Invalid bearer token."));
			return account.getRoles().stream()
					.<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
					.toList();
		};
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return converter;
	}

	@Bean
	AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
		return (request, response, exception) -> {
			response.setStatus(401);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(
					response.getOutputStream(),
					new ApiErrorResponse(
							401,
							"Unauthorized",
							"Authentication is required or credentials are invalid.",
							request.getRequestURI(),
							Map.of()));
		};
	}

	@Bean
	AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
		return (request, response, exception) -> {
			response.setStatus(403);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(
					response.getOutputStream(),
					new ApiErrorResponse(
							403,
							"Forbidden",
							"You are not authorized to access this resource.",
							request.getRequestURI(),
							Map.of()));
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			AuthenticationEntryPoint authenticationEntryPoint,
			AccessDeniedHandler accessDeniedHandler,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/api/v1/health",
								"/api/v1/auth/register",
								"/api/v1/auth/login",
								"/api/v1/auth/refresh",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/actuator/health")
						.permitAll()
						.requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.authenticationEntryPoint(authenticationEntryPoint)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}
}
