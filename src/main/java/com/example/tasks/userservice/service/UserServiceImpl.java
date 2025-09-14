package com.example.tasks.userservice.service;

import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.exception.DuplicateEmailException;
import com.example.tasks.userservice.exception.UserNotFoundException;
import com.example.tasks.userservice.mapper.UserMapper;
import com.example.tasks.userservice.model.User;
import com.example.tasks.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	@Value("${gateway.internal.secret}")
	private String internalServiceSecret;

	@Override
	@Transactional
	public UserResponseDto createUser(UserRequestDto userRequestDto, String id) {
		if (userRepository.existsByEmail(userRequestDto.getEmail())) {
			throw new DuplicateEmailException("Email already exists: " + userRequestDto.getEmail());
		}
		User user = userMapper.toEntity(userRequestDto);
		user.setId(UUID.fromString(id));
		User savedUser = userRepository.save(user);
		return userMapper.toResponseDto(savedUser);
	}

	@Override
	@Transactional
	@CacheEvict(value = "users", key = "#p0")
	public UserResponseDto updateUser(UUID id, UserRequestDto userRequestDto, String userIdFromToken) {
		if (!id.equals(UUID.fromString(userIdFromToken))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this user's data");
		}
		User existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
		if (!existingUser.getEmail().equals(userRequestDto.getEmail()) && userRepository.existsByEmail(userRequestDto.getEmail())) {
			throw new DuplicateEmailException("Email already exists: " + id);
		}
		userMapper.updateUserFromDto(userRequestDto, existingUser);
		User updatedUser = userRepository.save(existingUser);
		return userMapper.toResponseDto(updatedUser);
	}

	@Override
	@Transactional
	@CacheEvict(value = "users", key = "#p0")
	public void deleteUser(UUID id, String userIdFromToken, String userRoles) {
		UUID currentUserUUID = UUID.fromString(userIdFromToken);

		boolean isSelfDeletion = id.equals(currentUserUUID);
		boolean isAdmin = userRoles.contains("ROLE_ADMIN");

		if (!isSelfDeletion && !isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this user's data");
		}

		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(("User not found with id: " + id)));
		userRepository.delete(user);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#p0")
	public UserResponseDto getUserById(UUID id, String userIdFromToken) {
		if (!id.equals(UUID.fromString(userIdFromToken))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to see this user's data");
		}
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(("User not found with id: " + id)));
		return userMapper.toResponseDto(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponseDto> getAllUsers(String roles) {
		String[] array = roles.split(",");
		boolean isAdmin = Arrays.stream(array).anyMatch(role -> role.trim().contains("ADMIN"));
		if (!isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Required role: ADMIN");
		}
		List<User> users = userRepository.findAll();
		return users.stream().map(userMapper::toResponseDto).toList();
	}

	@Override
	@Transactional
	public List<UserResponseDto> getUsersByIds(List<UUID> ids, String roles) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		String[] array = roles.split(",");
		boolean isAdmin = Arrays.stream(array).anyMatch(role -> role.trim().equalsIgnoreCase("ROLE_ADMIN"));
		if (!isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Required role: ADMIN");
		}
		List<User> users = userRepository.findAllByIdIn(ids);
		return users.stream().map(userMapper::toResponseDto).toList();
	}
}
