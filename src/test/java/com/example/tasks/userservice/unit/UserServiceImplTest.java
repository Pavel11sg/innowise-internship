package com.example.tasks.userservice.unit;


import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.exception.DuplicateEmailException;
import com.example.tasks.userservice.exception.UserNotFoundException;
import com.example.tasks.userservice.mapper.UserMapper;
import com.example.tasks.userservice.model.User;
import com.example.tasks.userservice.repository.UserRepository;
import com.example.tasks.userservice.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserServiceImpl userService;

	private UserRequestDto createUserRequestDto() {
		UserRequestDto request = new UserRequestDto();
		request.setName("John");
		request.setSurname("Doe");
		request.setEmail("test@example.com");
		request.setBirthDate(LocalDate.of(1990, 1, 1));
		return request;
	}

	private User createUserEntity() {
		User user = new User();
		user.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		user.setName("John");
		user.setSurname("Doe");
		user.setEmail("test@example.com");
		user.setBirthDate(LocalDate.of(1990, 1, 1));
		return user;
	}

	private UserResponseDto createUserResponseDto() {
		UserResponseDto response = new UserResponseDto();
		response.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		response.setName("John");
		response.setSurname("Doe");
		response.setMaskedEmail("t***t@example.com");
		response.setBirthDate(LocalDate.of(1990, 1, 1));
		return response;
	}

	@Test
	void createUser_shouldReturnUserResponseDto_whenEmailIsUnique() {
		UserRequestDto request = createUserRequestDto();
		String userId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		User userEntity = new User();
		User savedUser = createUserEntity();
		UserResponseDto expectedResponse = createUserResponseDto();

		when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(userMapper.toEntity(request)).thenReturn(userEntity);
		when(userRepository.save(userEntity)).thenReturn(savedUser);
		when(userMapper.toResponseDto(savedUser)).thenReturn(expectedResponse);

		UserResponseDto result = userService.createUser(request, userId);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userRepository).existsByEmail("test@example.com");
		verify(userMapper).toEntity(request);
		verify(userRepository).save(userEntity);
		verify(userMapper).toResponseDto(savedUser);
	}

	@Test
	void createUser_ShouldThrowDuplicateEmailException_WhenEmailExists() {
		UserRequestDto request = createUserRequestDto();
		String userId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

		DuplicateEmailException exception = assertThrows(
				DuplicateEmailException.class,
				() -> userService.createUser(request, userId)
		);

		assertEquals("Email already exists: test@example.com", exception.getMessage());
		verify(userRepository).existsByEmail("test@example.com");
		verifyNoMoreInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void updateUser_ShouldUpdate_WhenEmailNotChangedAndAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		UserRequestDto request = createUserRequestDto();
		User existingUser = createUserEntity();
		UserResponseDto expectedResponse = createUserResponseDto();

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(existingUser)).thenReturn(existingUser);
		when(userMapper.toResponseDto(existingUser)).thenReturn(expectedResponse);

		UserResponseDto result = userService.updateUser(userId, request, userIdFromToken);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userRepository).findById(userId);
		verify(userRepository, never()).existsByEmail(any());
		verify(userMapper).updateUserFromDto(request, existingUser);
		verify(userRepository).save(existingUser);
		verify(userMapper).toResponseDto(existingUser);
	}

	@Test
	void updateUser_ShouldReturnUpdatedUser_WhenEmailChangedAndUniqueAndAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		UserRequestDto request = createUserRequestDto();
		request.setEmail("new@example.com");
		User existingUser = createUserEntity();
		existingUser.setEmail("old@example.com");
		UserResponseDto expectedResponse = createUserResponseDto();
		expectedResponse.setMaskedEmail("new@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
		when(userRepository.save(existingUser)).thenReturn(existingUser);
		when(userMapper.toResponseDto(existingUser)).thenReturn(expectedResponse);

		UserResponseDto result = userService.updateUser(userId, request, userIdFromToken);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userRepository).findById(userId);
		verify(userRepository).existsByEmail("new@example.com");
		verify(userMapper).updateUserFromDto(request, existingUser);
		verify(userRepository).save(existingUser);
		verify(userMapper).toResponseDto(existingUser);
	}

	@Test
	void updateUser_ShouldThrowDuplicateEmailException_WhenEmailChangedAndExists() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		UserRequestDto request = createUserRequestDto();
		request.setEmail("existing@example.com");
		User existingUser = createUserEntity();
		existingUser.setEmail("old@example.com");

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

		assertThrows(DuplicateEmailException.class, () -> {
			userService.updateUser(userId, request, userIdFromToken);
		});

		verify(userRepository).findById(userId);
		verify(userRepository).existsByEmail("existing@example.com");
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUser_ShouldThrowUserNotFoundException_WhenUserNotExists() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		UserRequestDto request = createUserRequestDto();

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		UserNotFoundException exception = assertThrows(
				UserNotFoundException.class,
				() -> userService.updateUser(userId, request, userIdFromToken)
		);

		assertEquals("User not found with id: f47ac10b-58cc-4372-a567-0e02b2c3d479", exception.getMessage());
		verify(userRepository).findById(userId);
		verifyNoMoreInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void updateUser_ShouldThrowForbiddenException_WhenNotAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d480";
		UserRequestDto request = createUserRequestDto();

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> userService.updateUser(userId, request, userIdFromToken)
		);

		assertEquals("403 FORBIDDEN \"You are not authorized to update this user's data\"", exception.getMessage());
		verifyNoInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void deleteUser_ShouldDeleteUser_WhenExistsAndSelfDeletion() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		String userRoles = "ROLE_USER";
		User user = createUserEntity();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		doNothing().when(userRepository).delete(user);

		userService.deleteUser(userId, userIdFromToken, userRoles);

		verify(userRepository).findById(userId);
		verify(userRepository).delete(user);
	}

	@Test
	void deleteUser_ShouldDeleteUser_WhenExistsAndAdmin() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d480";
		String userRoles = "ROLE_ADMIN";
		User user = createUserEntity();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		doNothing().when(userRepository).delete(user);

		userService.deleteUser(userId, userIdFromToken, userRoles);

		verify(userRepository).findById(userId);
		verify(userRepository).delete(user);
	}

	@Test
	void deleteUser_ShouldThrowForbiddenException_WhenNotAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d480";
		String userRoles = "ROLE_USER";

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> userService.deleteUser(userId, userIdFromToken, userRoles)
		);

		assertEquals("403 FORBIDDEN \"You are not authorized to delete this user's data\"", exception.getMessage());
		verifyNoInteractions(userRepository);
	}

	@Test
	void getUserById_ShouldReturnUser_WhenExistsAndAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
		User user = createUserEntity();
		UserResponseDto expectedResponse = createUserResponseDto();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toResponseDto(user)).thenReturn(expectedResponse);

		UserResponseDto result = userService.getUserById(userId, userIdFromToken);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userRepository).findById(userId);
		verify(userMapper).toResponseDto(user);
	}

	@Test
	void getUserById_ShouldThrowForbiddenException_WhenNotAuthorized() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		String userIdFromToken = "f47ac10b-58cc-4372-a567-0e02b2c3d480";

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> userService.getUserById(userId, userIdFromToken)
		);

		assertEquals("403 FORBIDDEN \"You are not authorized to see this user's data\"", exception.getMessage());
		verifyNoInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void getAllUsers_ShouldReturnListOfUsers_WhenAdmin() {
		User user = createUserEntity();
		UserResponseDto response = createUserResponseDto();
		String roles = "ROLE_ADMIN";

		when(userRepository.findAll()).thenReturn(List.of(user));
		when(userMapper.toResponseDto(user)).thenReturn(response);

		List<UserResponseDto> result = userService.getAllUsers(roles);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(response, result.get(0));
		verify(userRepository).findAll();
		verify(userMapper).toResponseDto(user);
	}

	@Test
	void getAllUsers_ShouldReturnEmptyList_WhenNoUsersAndAdmin() {
		String roles = "ROLE_ADMIN";
		when(userRepository.findAll()).thenReturn(Collections.emptyList());

		List<UserResponseDto> result = userService.getAllUsers(roles);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(userRepository).findAll();
		verifyNoInteractions(userMapper);
	}

	@Test
	void getAllUsers_ShouldThrowForbiddenException_WhenNotAdmin() {
		String roles = "ROLE_USER";

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> userService.getAllUsers(roles)
		);

		assertEquals("403 FORBIDDEN \"Access denied. Required role: ADMIN\"", exception.getMessage());
		verifyNoInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void getUsersByIds_ShouldReturnUsers_WhenAdmin() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		List<UUID> ids = List.of(userId);
		String roles = "ROLE_ADMIN";
		User user = createUserEntity();
		UserResponseDto response = createUserResponseDto();

		when(userRepository.findAllByIdIn(ids)).thenReturn(List.of(user));
		when(userMapper.toResponseDto(user)).thenReturn(response);

		List<UserResponseDto> result = userService.getUsersByIds(ids, roles);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(response, result.get(0));
		verify(userRepository).findAllByIdIn(ids);
		verify(userMapper).toResponseDto(user);
	}

	@Test
	void getUsersByIds_ShouldReturnEmptyList_WhenNoIdsAndAdmin() {
		String roles = "ROLE_ADMIN";
		List<UserResponseDto> result = userService.getUsersByIds(Collections.emptyList(), roles);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verifyNoInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}

	@Test
	void getUsersByIds_ShouldThrowForbiddenException_WhenNotAdmin() {
		UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
		List<UUID> ids = List.of(userId);
		String roles = "ROLE_USER";

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> userService.getUsersByIds(ids, roles)
		);

		assertEquals("403 FORBIDDEN \"Access denied. Required role: ADMIN\"", exception.getMessage());
		verifyNoInteractions(userRepository);
		verifyNoInteractions(userMapper);
	}
}