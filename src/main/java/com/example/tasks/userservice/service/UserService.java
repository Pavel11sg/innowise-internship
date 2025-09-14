package com.example.tasks.userservice.service;

import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
	UserResponseDto createUser(UserRequestDto userRequestDto, String id);

	UserResponseDto updateUser(UUID id, UserRequestDto userRequestDto, String userIdFromToken);

	void deleteUser(UUID id, String userId, String userRoles);

	UserResponseDto getUserById(UUID id, String userId);

	List<UserResponseDto> getAllUsers(String roles);

	List<UserResponseDto> getUsersByIds(List<UUID> ids, String roles);
}
