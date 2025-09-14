package com.example.tasks.userservice.integration.controller;

import com.example.tasks.userservice.controller.UserController;
import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.exception.DuplicateEmailException;
import com.example.tasks.userservice.exception.UserNotFoundException;
import com.example.tasks.userservice.integration.utils.TestUserFactory;
import com.example.tasks.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	private static final String USER_ID = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
	private static final String ADMIN_ROLES = "ROLE_ADMIN,ROLE_USER";
	private static final String USER_ROLES = "ROLE_USER";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@Test
	void createUser_ReturnsCreated() throws Exception {
		UserRequestDto request = TestUserFactory.createUserRequestDto();
		UserResponseDto response = TestUserFactory.createUserResponseDto();
		when(userService.createUser(any(UserRequestDto.class), eq(USER_ID))).thenReturn(response);
		mockMvc.perform(post("/userservice/users")
						.header("X-User-ID", USER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(USER_ID))
				.andExpect(jsonPath("$.maskedEmail").value("t***************m@example.com"));
	}

	@Test
	void createUser_ThrowsDuplicateEmail() throws Exception {
		UserRequestDto request = TestUserFactory.createUserRequestDto("duplicate@example.com");
		when(userService.createUser(any(UserRequestDto.class), eq(USER_ID)))
				.thenThrow(new DuplicateEmailException("Email already exists"));
		mockMvc.perform(post("/userservice/users")
						.header("X-User-ID", USER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void getUserById_ReturnsUser() throws Exception {
		UserResponseDto response = TestUserFactory.createUserResponseDto();
		when(userService.getUserById(eq(UUID.fromString(USER_ID)), eq(USER_ID))).thenReturn(response);
		mockMvc.perform(get("/userservice/users/" + USER_ID)
						.header("X-User-ID", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USER_ID))
				.andExpect(jsonPath("$.maskedEmail").value("t***************m@example.com"));
	}

	@Test
	void getUserById_ThrowsNotFound() throws Exception {
		String nonExistentUserId = "f47ac10b-58cc-4372-a567-0e02b2c3d485";
		when(userService.getUserById(eq(UUID.fromString(nonExistentUserId)), eq(USER_ID)))
				.thenThrow(new UserNotFoundException("User not found"));
		mockMvc.perform(get("/userservice/users/" + nonExistentUserId)
						.header("X-User-ID", USER_ID))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateUser_ReturnsUpdatedUser() throws Exception {
		UserRequestDto request = TestUserFactory.createUserRequestDto();
		UserResponseDto response = TestUserFactory.createUserResponseDto();
		when(userService.updateUser(eq(UUID.fromString(USER_ID)), any(UserRequestDto.class), eq(USER_ID)))
				.thenReturn(response);
		mockMvc.perform(put("/userservice/users/" + USER_ID)
						.header("X-User-ID", USER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.maskedEmail").value("t***************m@example.com"));
	}

	@Test
	void getAllUsers_ReturnsList() throws Exception {
		List<UserResponseDto> users = List.of(
				TestUserFactory.createUserResponseDto(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), "u***1@example.com"),
				TestUserFactory.createUserResponseDto(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d480"), "u***2@example.com")
		);
		when(userService.getAllUsers(ADMIN_ROLES)).thenReturn(users);
		mockMvc.perform(get("/userservice/users")
						.header("X-User-Roles", ADMIN_ROLES))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].maskedEmail").value("u***1@example.com"))
				.andExpect(jsonPath("$[1].maskedEmail").value("u***2@example.com"));
	}

	@Test
	void deleteUser_ShouldReturnNoContent() throws Exception {
		UUID userId = UUID.fromString(USER_ID);
		doNothing().when(userService).deleteUser(userId, USER_ID, ADMIN_ROLES);
		mockMvc.perform(delete("/userservice/users/{id}", USER_ID)
						.header("X-User-ID", USER_ID)
						.header("X-User-Roles", ADMIN_ROLES))
				.andExpect(status().isNoContent());
		verify(userService).deleteUser(userId, USER_ID, ADMIN_ROLES);
	}

	@Test
	void getUsersByIds_ReturnsUsers() throws Exception {
		List<UserResponseDto> users = List.of(
				TestUserFactory.createUserResponseDto(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), "u***1@example.com"),
				TestUserFactory.createUserResponseDto(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d480"), "u***2@example.com")
		);
		when(userService.getUsersByIds(any(List.class), eq(ADMIN_ROLES))).thenReturn(users);
		mockMvc.perform(get("/userservice/users/batch")
						.param("id", "f47ac10b-58cc-4372-a567-0e02b2c3d479", "f47ac10b-58cc-4372-a567-0e02b2c3d480")
						.header("X-User-Roles", ADMIN_ROLES))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].maskedEmail").value("u***1@example.com"))
				.andExpect(jsonPath("$[1].maskedEmail").value("u***2@example.com"));
	}
}