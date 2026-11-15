package com.astrahealth.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.astrahealth.backend.dto.request.UpdateAstronautRequest;
import com.astrahealth.backend.dto.response.AstronautResponse;
import com.astrahealth.backend.entity.Astronaut;
import com.astrahealth.backend.exception.DuplicateResourceException;
import com.astrahealth.backend.exception.GlobalExceptionHandler;
import com.astrahealth.backend.exception.ResourceNotFoundException;
import com.astrahealth.backend.service.AstronautService;

@ExtendWith(MockitoExtension.class)
class AstronautControllerTests {

	@Mock
	private AstronautService astronautService;

	@InjectMocks
	private AstronautController astronautController;

	private LocalValidatorFactoryBean validator;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() throws Exception {
		validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(astronautController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@AfterEach
	void tearDown() {
		validator.close();
	}

	@Test
	void createReturnsCreatedStatusAndResponseDto() throws Exception {
		when(astronautService.createAstronaut(any(Astronaut.class))).thenAnswer(invocation -> {
			Astronaut astronaut = invocation.getArgument(0);
			astronaut.setId(7L);
			return astronaut;
		});

		mockMvc.perform(post("/api/v1/astronauts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/astronauts/7"))
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.astronautCode").value("AST-007"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void createAllowsEmailToBeOmitted() throws Exception {
		when(astronautService.createAstronaut(any(Astronaut.class))).thenAnswer(invocation -> {
			Astronaut astronaut = invocation.getArgument(0);
			astronaut.setId(8L);
			return astronaut;
		});

		mockMvc.perform(post("/api/v1/astronauts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"astronautCode":"AST-008","fullName":"Test Astronaut","status":"ACTIVE"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void invalidRequestReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/astronauts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"astronautCode":" ","fullName":"","email":"invalid","status":"ACTIVE"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.astronautCode").exists())
				.andExpect(jsonPath("$.validationErrors.email").exists());
	}

	@Test
	void duplicateResourceReturnsConflict() throws Exception {
		when(astronautService.createAstronaut(any(Astronaut.class)))
				.thenThrow(new DuplicateResourceException("Astronaut code already exists."));

		mockMvc.perform(post("/api/v1/astronauts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequest()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Astronaut code already exists."));
	}

	@Test
	void listReturnsPaginationMetadataAndResponseDtos() throws Exception {
		Astronaut astronaut = createAstronaut();
		PageRequest expectedPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "astronautCode"));
		when(astronautService.getAllAstronauts(any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(astronaut), expectedPageable, 1));

		mockMvc.perform(get("/api/v1/astronauts")
						.param("page", "0")
						.param("size", "1")
						.param("sortBy", "astronautCode")
						.param("direction", "DESC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].astronautCode").value("AST-007"))
				.andExpect(jsonPath("$.content[0].password").doesNotExist());

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(astronautService).getAllAstronauts(pageableCaptor.capture());
		assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("astronautCode").getDirection());
	}

	@Test
	void pageSizeAboveMaximumReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts").param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Page size must be between 1 and 100."));
	}

	@Test
	void missingAstronautReturnsNotFound() throws Exception {
		when(astronautService.getAstronautById(99L))
				.thenThrow(new ResourceNotFoundException("Astronaut not found with ID: 99"));

		mockMvc.perform(get("/api/v1/astronauts/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.path").value("/api/v1/astronauts/99"))
				.andExpect(jsonPath("$.validationErrors", aMapWithSize(0)));
	}

	@Test
	void existingAstronautReturnsResponseDtoAndDetails() throws Exception {
		when(astronautService.getAstronautById(7L)).thenReturn(createAstronaut());

		assertInstanceOf(
				AstronautResponse.class,
				astronautController.getAstronautById(7L));

		mockMvc.perform(get("/api/v1/astronauts/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", aMapWithSize(5)))
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.astronautCode").value("AST-007"))
				.andExpect(jsonPath("$.fullName").value("Test Astronaut"))
				.andExpect(jsonPath("$.email").value("test@example.com"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void zeroAstronautIdReturnsConsistentBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts/0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."))
				.andExpect(jsonPath("$.path").value("/api/v1/astronauts/0"))
				.andExpect(jsonPath("$.validationErrors", aMapWithSize(0)));
	}

	@Test
	void negativeAstronautIdReturnsConsistentBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/astronauts/-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."))
				.andExpect(jsonPath("$.path").value("/api/v1/astronauts/-1"))
				.andExpect(jsonPath("$.validationErrors", aMapWithSize(0)));
	}

	@Test
	void updateReturnsUpdatedResponseDtoAndPreservesId() throws Exception {
		Astronaut updatedAstronaut = createAstronaut();
		updatedAstronaut.setFullName("Alex Morgan Updated");
		updatedAstronaut.setEmail("alex.updated@example.com");
		when(astronautService.updateAstronaut(eq(7L), any(Astronaut.class)))
				.thenReturn(updatedAstronaut);

		assertInstanceOf(
				AstronautResponse.class,
				astronautController.updateAstronaut(7L, createUpdateRequest()));

		mockMvc.perform(put("/api/v1/astronauts/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", aMapWithSize(5)))
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.astronautCode").value("AST-007"))
				.andExpect(jsonPath("$.fullName").value("Alex Morgan Updated"))
				.andExpect(jsonPath("$.email").value("alex.updated@example.com"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void updateWithDuplicateAstronautCodeReturnsConflict() throws Exception {
		when(astronautService.updateAstronaut(eq(7L), any(Astronaut.class)))
				.thenThrow(new DuplicateResourceException("Astronaut code already exists."));

		mockMvc.perform(put("/api/v1/astronauts/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Astronaut code already exists."));
	}

	@Test
	void updateWithDuplicateEmailReturnsConflict() throws Exception {
		when(astronautService.updateAstronaut(eq(7L), any(Astronaut.class)))
				.thenThrow(new DuplicateResourceException("Email already exists."));

		mockMvc.perform(put("/api/v1/astronauts/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Email already exists."));
	}

	@Test
	void updatingMissingAstronautReturnsNotFound() throws Exception {
		when(astronautService.updateAstronaut(eq(99L), any(Astronaut.class)))
				.thenThrow(new ResourceNotFoundException("Astronaut not found with ID: 99"));

		mockMvc.perform(put("/api/v1/astronauts/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.path").value("/api/v1/astronauts/99"));
	}

	@Test
	void updateWithZeroIdReturnsBadRequest() throws Exception {
		mockMvc.perform(put("/api/v1/astronauts/0")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."));

		verify(astronautService, never()).updateAstronaut(any(Long.class), any(Astronaut.class));
	}

	@Test
	void updateWithNegativeIdReturnsBadRequest() throws Exception {
		mockMvc.perform(put("/api/v1/astronauts/-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validUpdateRequest()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."));

		verify(astronautService, never()).updateAstronaut(any(Long.class), any(Astronaut.class));
	}

	@Test
	void updateWithInvalidRequestBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(put("/api/v1/astronauts/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"astronautCode":" ","email":"invalid","status":""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.validationErrors.astronautCode").exists())
				.andExpect(jsonPath("$.validationErrors.fullName").exists())
				.andExpect(jsonPath("$.validationErrors.email").exists())
				.andExpect(jsonPath("$.validationErrors.status").exists());
	}

	@Test
	void updateWithMalformedJsonReturnsBadRequest() throws Exception {
		mockMvc.perform(put("/api/v1/astronauts/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Request body or parameter is invalid."));
	}

	@Test
	void unexpectedFailureReturnsGenericServerError() throws Exception {
		when(astronautService.getAstronautById(99L))
				.thenThrow(new RuntimeException("Sensitive database details"));

		mockMvc.perform(get("/api/v1/astronauts/99"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("An unexpected server error occurred."))
				.andExpect(content().string(org.hamcrest.Matchers.not(
						org.hamcrest.Matchers.containsString("Sensitive database details"))));
	}

	@Test
	void deleteReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/v1/astronauts/7"))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(astronautService).deleteAstronaut(7L);
	}

	@Test
	void deletingMissingAstronautReturnsNotFound() throws Exception {
		doThrow(new ResourceNotFoundException("Astronaut not found with ID: 99"))
				.when(astronautService).deleteAstronaut(99L);

		mockMvc.perform(delete("/api/v1/astronauts/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.path").value("/api/v1/astronauts/99"));
	}

	@Test
	void deleteWithZeroIdReturnsBadRequestWithoutCallingService() throws Exception {
		mockMvc.perform(delete("/api/v1/astronauts/0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."));

		verify(astronautService, never()).deleteAstronaut(any(Long.class));
	}

	@Test
	void deleteWithNegativeIdReturnsBadRequestWithoutCallingService() throws Exception {
		mockMvc.perform(delete("/api/v1/astronauts/-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Astronaut ID must be a positive number."));

		verify(astronautService, never()).deleteAstronaut(any(Long.class));
	}

	@Test
	void dataIntegrityFailureReturnsGenericConflictWithoutDatabaseDetails() throws Exception {
		doThrow(new DataIntegrityViolationException("Sensitive database details"))
				.when(astronautService).deleteAstronaut(7L);

		mockMvc.perform(delete("/api/v1/astronauts/7"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("The operation conflicts with existing data constraints."))
				.andExpect(content().string(org.hamcrest.Matchers.not(
						org.hamcrest.Matchers.containsString("Sensitive database details"))));
	}

	private String validRequest() {
		return """
				{"astronautCode":"AST-007","fullName":"Test Astronaut","email":"test@example.com","status":"ACTIVE"}
				""";
	}

	private String validUpdateRequest() {
		return """
				{"astronautCode":"AST-007","fullName":"Alex Morgan Updated","email":"alex.updated@example.com","status":"ACTIVE"}
				""";
	}

	private UpdateAstronautRequest createUpdateRequest() {
		UpdateAstronautRequest request = new UpdateAstronautRequest();
		request.setAstronautCode("AST-007");
		request.setFullName("Alex Morgan Updated");
		request.setEmail("alex.updated@example.com");
		request.setStatus("ACTIVE");
		return request;
	}

	private Astronaut createAstronaut() {
		Astronaut astronaut = new Astronaut();
		astronaut.setId(7L);
		astronaut.setAstronautCode("AST-007");
		astronaut.setFullName("Test Astronaut");
		astronaut.setEmail("test@example.com");
		astronaut.setStatus("ACTIVE");
		return astronaut;
	}
}
