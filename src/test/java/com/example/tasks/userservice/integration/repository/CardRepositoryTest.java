package com.example.tasks.userservice.integration.repository;

import com.example.tasks.userservice.integration.utils.TestCardFactory;
import com.example.tasks.userservice.integration.utils.TestUserFactory;
import com.example.tasks.userservice.model.Card;
import com.example.tasks.userservice.model.User;
import com.example.tasks.userservice.repository.CardRepository;
import com.example.tasks.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardRepositoryTest {
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private UserRepository userRepository;
	@AfterEach
	void tearDown() {
		cardRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void findValidCardsByUserId_ShouldReturnListOfCards() {
		User user = TestUserFactory.createUserEntity();
		Card card1 = TestCardFactory.createCardEntity();
		Card card2 = TestCardFactory.createCardEntity("2111111111111111");
		card1.setUser(user);
		card2.setUser(user);
		user.setCards(List.of(card1,card2));
		User savedUser = userRepository.save(user);

		List<Card> cards = cardRepository.findAllByUserId(savedUser.getId());

		assertThat(cards).hasSize(2);
		assertThat(cards).extracting(Card::getUser).containsOnly(savedUser);
		assertThat(cards).extracting(Card::getNumber).containsExactlyInAnyOrder(card1.getNumber(), card2.getNumber());
	}

	@Test
	@Transactional
	void shouldUpdateCardHolder_WhenUserIdProvided() {
		User user = TestUserFactory.createUserEntity();
		Card card = TestCardFactory.createCardEntity();
		card.setUser(user);
		user.setCards(List.of(card));

		User savedUser = userRepository.save(user);
		Card savedCard = savedUser.getCards().get(0);

		System.out.println("User ID: " + savedUser.getId());
		System.out.println("Card ID: " + savedCard.getId());

		String newHolder = "NEW HOLDER";
		int updatedRows = cardRepository.updateCardHolderByUser(savedUser.getId(), newHolder);

		cardRepository.flush();

		List<Card> allCards = cardRepository.findAll();
		System.out.println("All cards: " + allCards.size());

		List<Card> userCards = cardRepository.findAllByUserId(savedUser.getId());
		System.out.println("User cards: " + userCards.size());

		Optional<Card> updatedCard = cardRepository.findByUserIdAndId(savedUser.getId(), savedCard.getId());

		System.out.println("Updated card present: " + updatedCard.isPresent());

		assertThat(updatedCard).isPresent().withFailMessage("Card should be found by userId and cardId");
		assertThat(updatedCard.get().getHolder()).isEqualTo(newHolder);
	}
}
