package com.example.tasks.userservice.integration.utils;

import com.example.tasks.userservice.dto.CardResponseDto;
import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestUserFactory {

	public static User createUserEntity() {
		User user = new User();
		user.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		user.setName("testName");
		user.setSurname("testSurname");
		user.setEmail("testNametestSurname@example.com");
		user.setBirthDate(LocalDate.of(1990, 1, 1));
		return user;
	}

	public static UserRequestDto createUserRequestDto() {
		UserRequestDto userRequestDto = new UserRequestDto();
		userRequestDto.setName("testName");
		userRequestDto.setSurname("testSurname");
		userRequestDto.setEmail("testNametestSurname@example.com");
		userRequestDto.setBirthDate(LocalDate.of(1990, 1, 1));
		return userRequestDto;
	}

	public static UserRequestDto createUserRequestDto(String email) {
		UserRequestDto dto = new UserRequestDto();
		dto.setName("testName");
		dto.setSurname("testSurname");
		dto.setEmail(email);
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		return dto;
	}

	public static UserResponseDto createUserResponseDto() {
		CardResponseDto cardResponseDto = new CardResponseDto();
		cardResponseDto.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		cardResponseDto.setLastFourDigits("1111");
		cardResponseDto.setMaskedHolder("testName t**********");
		cardResponseDto.setExpirationDate(LocalDate.now().plusYears(1));
		List<CardResponseDto> cards = new ArrayList<>();
		cards.add(cardResponseDto);
		UserResponseDto userResponseDto = new UserResponseDto();
		userResponseDto.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		userResponseDto.setName("testName");
		userResponseDto.setSurname("testSurname");
		userResponseDto.setMaskedEmail("t***************m@example.com");
		userResponseDto.setBirthDate(LocalDate.of(1990, 1, 1));
		userResponseDto.setCards(cards);
		return userResponseDto;
	}

	public static UserResponseDto createUserResponseDto(UUID id, String maskedEmail) {
		UserResponseDto userResponseDto = createUserResponseDto();
		userResponseDto.setId(id);
		userResponseDto.setMaskedEmail(maskedEmail);
		return userResponseDto;
	}

	public static String maskEmail(String email) {
		if (email == null || email.length() < 5) return email;
		int atIndex = email.indexOf('@');
		if (atIndex < 2) return email;
		return email.charAt(0) + "***" + email.substring(atIndex - 1);
	}
}