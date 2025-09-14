package com.example.tasks.userservice.service;

import com.example.tasks.userservice.dto.CardRequestDto;
import com.example.tasks.userservice.dto.CardResponseDto;

import java.util.List;
import java.util.UUID;

public interface CardService {
	CardResponseDto createCard(UUID userId, CardRequestDto cardRequestDto);
	CardResponseDto updateCard(UUID userId, UUID cardId, CardRequestDto cardRequestDto);
	CardResponseDto getCardByUserIdAndCardId(UUID UserId, UUID CardId);
	List<CardResponseDto> getAllCardsByUserId(UUID userId);
	void deleteCard(UUID userId, UUID cardId);
}
