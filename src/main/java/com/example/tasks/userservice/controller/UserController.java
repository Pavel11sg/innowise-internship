package com.example.tasks.userservice.controller;

import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/userservice/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;


	@PostMapping
	public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto,
													  @RequestHeader("X-User-ID") String userId) {
		UserResponseDto createdUser = userService.createUser(userRequestDto, userId);
		return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(@Valid @PathVariable(name = "id") UUID id,
													  @RequestBody UserRequestDto userRequestDto,
													  @RequestHeader("X-User-ID") String userId) {
		UserResponseDto updatedUser = userService.updateUser(id, userRequestDto, userId);
		return ResponseEntity.ok(updatedUser);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") UUID id,
										   @RequestHeader("X-User-ID") String userId,
										   @RequestHeader("X-User-Roles") String roles) {
		userService.deleteUser(id, userId, roles);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable(name = "id") UUID id,
													   @RequestHeader("X-User-ID") String userId) {
		UserResponseDto user = userService.getUserById(id, userId);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/batch")
	public ResponseEntity<List<UserResponseDto>> getUsersByIds(@RequestParam(name = "id") List<UUID> ids,
															   @RequestHeader("X-User-Roles") String roles) {
		List<UserResponseDto> users = userService.getUsersByIds(ids, roles);
		return ResponseEntity.ok(users);
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDto>> getAllUsers(@RequestHeader("X-User-Roles") String roles) {
		List<UserResponseDto> users = userService.getAllUsers(roles);
		return ResponseEntity.ok(users);
	}
}
