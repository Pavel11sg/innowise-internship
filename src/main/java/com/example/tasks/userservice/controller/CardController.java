package com.example.tasks.userservice.controller;

import com.example.tasks.userservice.dto.CardRequestDto;
import com.example.tasks.userservice.dto.CardResponseDto;
import com.example.tasks.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/userservice/users/{userId}/cards")
@RequiredArgsConstructor
public class CardController {
	private final CardService cardService;

	@PostMapping
	public ResponseEntity<CardResponseDto> createCard(@PathVariable(name = "userId") UUID userId, @Valid @RequestBody CardRequestDto cardRequestDto) {
		CardResponseDto card = cardService.createCard(userId, cardRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(card);
	}

	@PutMapping("/{cardId}")
	public ResponseEntity<CardResponseDto> updateCard(@PathVariable(name = "userId") UUID userId,
													  @PathVariable(name = "cardId") UUID cardId,
													  @Valid @RequestBody CardRequestDto cardRequestDto) {
		CardResponseDto card = cardService.updateCard(userId, cardId, cardRequestDto);
		return ResponseEntity.ok(card);
	}

	@GetMapping("/{cardId}")
	public ResponseEntity<CardResponseDto> getCard(@PathVariable(name = "userId") UUID userId, @PathVariable(name = "cardId") UUID cardId) {
		return ResponseEntity.ok(cardService.getCardByUserIdAndCardId(userId, cardId));
	}

	@GetMapping()
	public ResponseEntity<List<CardResponseDto>> getCards(@PathVariable(name = "userId") UUID userId) {
		return ResponseEntity.ok(cardService.getAllCardsByUserId(userId));
	}
	@DeleteMapping("/{cardId}")
	public ResponseEntity<Void> deleteCard(@PathVariable(name = "userId") UUID userId, @PathVariable(name = "cardId") UUID cardId) {
		cardService.deleteCard(userId, cardId);
		return ResponseEntity.noContent().build();
	}
}
