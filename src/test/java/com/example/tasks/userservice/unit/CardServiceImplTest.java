package com.example.tasks.userservice.unit;

import com.example.tasks.userservice.dto.CardRequestDto;
import com.example.tasks.userservice.dto.CardResponseDto;
import com.example.tasks.userservice.exception.CardNotFoundException;
import com.example.tasks.userservice.exception.CardNotOwnedException;
import com.example.tasks.userservice.exception.DuplicateCardException;
import com.example.tasks.userservice.exception.UserNotFoundException;
import com.example.tasks.userservice.mapper.CardMapper;
import com.example.tasks.userservice.model.Card;
import com.example.tasks.userservice.model.User;
import com.example.tasks.userservice.repository.CardRepository;
import com.example.tasks.userservice.repository.UserRepository;
import com.example.tasks.userservice.service.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CardRepository cardRepository;

	@Mock
	private CardMapper cardMapper;

	@InjectMocks
	private CardServiceImpl cardService;

	private User createUser() {
		User user = new User();
		user.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		return user;
	}

	private Card createCard() {
		Card card = new Card();
		card.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		card.setNumber("1234567890123456");
		card.setHolder("IVAN PETROV");
		card.setExpirationDate(LocalDate.now().plusYears(1));
		card.setUser(createUser());
		return card;
	}

	private CardRequestDto createCardRequestDto() {
		CardRequestDto request = new CardRequestDto();
		request.setNumber("1234567890123456");
		request.setHolder("IVAN PETROV");
		request.setExpirationDate(LocalDate.now().plusYears(1));
		return request;
	}

	private CardResponseDto createCardResponseDto() {
		CardResponseDto response = new CardResponseDto();
		response.setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		response.setLastFourDigits("3456");
		response.setMaskedHolder("IVAN P***");
		response.setExpirationDate(LocalDate.now().plusYears(1));
		return response;
	}

	@Test
	void createCard_shouldReturnCardResponse_whenDataValid() {
		User user = createUser();
		CardRequestDto request = createCardRequestDto();
		Card card = createCard();
		Card savedCard = createCard();
		CardResponseDto expectedResponse = createCardResponseDto();

		when(userRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(user));
		when(cardRepository.existsByUserAndNumber(user, "1234567890123456")).thenReturn(false);
		when(cardMapper.toEntity(request)).thenReturn(card);
		when(cardRepository.save(card)).thenReturn(savedCard);
		when(cardMapper.toResponseDto(savedCard)).thenReturn(expectedResponse);

		CardResponseDto result = cardService.createCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(userRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).existsByUserAndNumber(user, "1234567890123456");
		verify(cardMapper).toEntity(request);
		verify(cardRepository).save(card);
		verify(cardMapper).toResponseDto(savedCard);
	}

	@Test
	void createCard_shouldThrowUserNotFoundException_whenUserNotExists() {
		CardRequestDto request = createCardRequestDto();
		when(userRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> cardService.createCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request));
		verify(userRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoMoreInteractions(userRepository);
		verifyNoInteractions(cardRepository, cardMapper);
	}

	@Test
	void createCard_shouldThrowDuplicateCardException_whenCardExists() {
		User user = createUser();
		CardRequestDto request = createCardRequestDto();

		when(userRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(user));
		when(cardRepository.existsByUserAndNumber(user, "1234567890123456")).thenReturn(true);

		assertThrows(DuplicateCardException.class, () -> cardService.createCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request));
		verify(userRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).existsByUserAndNumber(user, "1234567890123456");
		verifyNoMoreInteractions(cardRepository);
		verifyNoInteractions(cardMapper);
	}

	@Test
	void updateCard_shouldReturnUpdatedCard_whenDataValid() {
		Card card = createCard();
		CardRequestDto request = createCardRequestDto();
		CardResponseDto expectedResponse = createCardResponseDto();

		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(card));
		when(cardRepository.save(card)).thenReturn(card);
		when(cardMapper.toResponseDto(card)).thenReturn(expectedResponse);

		CardResponseDto result = cardService.updateCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request);

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardMapper).updateFromDto(card, request);
		verify(cardRepository).save(card);
		verify(cardMapper).toResponseDto(card);
	}

	@Test
	void updateCard_shouldThrowCardNotFoundException_whenCardNotExists() {
		CardRequestDto request = createCardRequestDto();
		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.empty());

		assertThrows(CardNotFoundException.class, () -> cardService.updateCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request));
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoMoreInteractions(cardRepository);
		verifyNoInteractions(cardMapper);
	}

	@Test
	void updateCard_shouldThrowCardNotOwnedException_whenUserNotOwner() {
		Card card = createCard();
		card.getUser().setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d480"));
		CardRequestDto request = createCardRequestDto();

		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(card));

		assertThrows(CardNotOwnedException.class, () -> cardService.updateCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), request));
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoMoreInteractions(cardRepository);
		verifyNoInteractions(cardMapper);
	}

	@Test
	void getCardByUserIdAndCardId_shouldReturnCard_whenExists() {
		Card card = createCard();
		CardResponseDto expectedResponse = createCardResponseDto();

		when(cardRepository.findByUserIdAndId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(card));
		when(cardMapper.toResponseDto(card)).thenReturn(expectedResponse);

		CardResponseDto result = cardService.getCardByUserIdAndCardId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));

		assertNotNull(result);
		assertEquals(expectedResponse, result);
		verify(cardRepository).findByUserIdAndId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardMapper).toResponseDto(card);
	}

	@Test
	void getCardByUserIdAndCardId_shouldThrowCardNotFoundException_whenNotExists() {
		when(cardRepository.findByUserIdAndId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.empty());

		assertThrows(CardNotFoundException.class, () -> cardService.getCardByUserIdAndCardId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
		verify(cardRepository).findByUserIdAndId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoInteractions(cardMapper);
	}

	@Test
	void getAllCardsByUserId_shouldReturnCardsList_whenUserExists() {
		Card card = createCard();
		CardResponseDto response = createCardResponseDto();
		List<Card> cards = List.of(card);

		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(true);
		when(cardRepository.findAllByUserId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(cards);
		when(cardMapper.toResponseDto(card)).thenReturn(response);

		List<CardResponseDto> result = cardService.getAllCardsByUserId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(response, result.get(0));
		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).findAllByUserId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardMapper).toResponseDto(card);
	}

	@Test
	void getAllCardsByUserId_shouldThrowUserNotFoundException_whenUserNotExists() {
		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(false);

		assertThrows(UserNotFoundException.class, () -> cardService.getAllCardsByUserId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoInteractions(cardRepository, cardMapper);
	}

	@Test
	void deleteCard_shouldDeleteCard_whenUserIsOwner() {
		Card card = createCard();

		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(true);
		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(card));
		doNothing().when(cardRepository).delete(card);

		cardService.deleteCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));

		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).delete(card);
	}

	@Test
	void deleteCard_shouldThrowUserNotFoundException_whenUserNotExists() {
		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(false);

		assertThrows(UserNotFoundException.class, () -> cardService.deleteCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoInteractions(cardRepository);
	}

	@Test
	void deleteCard_shouldThrowCardNotFoundException_whenCardNotExists() {
		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(true);
		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.empty());

		assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoMoreInteractions(cardRepository);
	}

	@Test
	void deleteCard_shouldThrowCardNotOwnedException_whenUserNotOwner() {
		Card card = createCard();
		card.getUser().setId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d480"));

		when(userRepository.existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(true);
		when(cardRepository.findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))).thenReturn(Optional.of(card));

		assertThrows(CardNotOwnedException.class, () -> cardService.deleteCard(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"), UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")));
		verify(userRepository).existsById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verify(cardRepository).findById(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
		verifyNoMoreInteractions(cardRepository);
	}
}
