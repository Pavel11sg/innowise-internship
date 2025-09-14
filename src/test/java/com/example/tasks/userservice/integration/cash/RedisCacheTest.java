package com.example.tasks.userservice.integration.cash;

import com.example.tasks.userservice.dto.CardRequestDto;
import com.example.tasks.userservice.dto.CardResponseDto;
import com.example.tasks.userservice.dto.UserRequestDto;
import com.example.tasks.userservice.dto.UserResponseDto;
import com.example.tasks.userservice.integration.utils.TestCardFactory;
import com.example.tasks.userservice.integration.utils.TestUserFactory;
import com.example.tasks.userservice.service.CardService;
import com.example.tasks.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "classpath:/sql/cleanup.sql",
		executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RedisCacheTest {
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
	@Container
	@ServiceConnection
	private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	@Autowired
	private UserService userService;
	@Autowired
	private CardService cardService;
	@Autowired
	private CacheManager cacheManager;

	private String adminRoles = "ROLE_ADMIN";
	private String userRoles = "ROLE_USER";

	@BeforeEach
	@Transactional
	void setUp() {
		cacheManager.getCache("users").clear();
		cacheManager.getCache("cards").clear();
	}

	@Test
	void getUserById_ShouldCacheResult() {
		UserRequestDto request = TestUserFactory.createUserRequestDto();
		String userId = UUID.randomUUID().toString();
		UserResponseDto user = userService.createUser(request, userId);
		UUID userIdUUID = user.getId();

		UserResponseDto firstCall = userService.getUserById(userIdUUID, userId);
		assertNotNull(firstCall);

		UserResponseDto secondCall = userService.getUserById(userIdUUID, userId);
		assertNotNull(secondCall);

		assertNotNull(cacheManager.getCache("users").get(userIdUUID));
		assertEquals(firstCall, secondCall);
	}

	@Test
	void updateUser_ShouldEvictResult() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto("user@example.com");
		UserRequestDto updatedUserRequestDto = TestUserFactory.createUserRequestDto("updatedUser@example.com");
		String userId = UUID.randomUUID().toString();
		UserResponseDto userResponseDto = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = userResponseDto.getId();

		userService.getUserById(userIdUUID, userId);
		UserResponseDto cashedUser = (UserResponseDto) cacheManager.getCache("users").get(userIdUUID).get();
		assertNotNull(cashedUser);
		assertEquals(TestUserFactory.maskEmail(userRequestDto.getEmail()), cashedUser.getMaskedEmail());

		UserResponseDto updatedUserResponseDto = userService.updateUser(userIdUUID, updatedUserRequestDto, userId);
		assertNotNull(updatedUserResponseDto);
		assertNull(cacheManager.getCache("users").get(userIdUUID));
	}

	@Test
	void deleteUser_ShouldEvictResult() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto("user@example.com");
		String userId = UUID.randomUUID().toString();
		UserResponseDto userResponseDto = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = userResponseDto.getId();

		userService.getUserById(userIdUUID, userId);
		UserResponseDto cashedUser = (UserResponseDto) cacheManager.getCache("users").get(userIdUUID).get();
		assertNotNull(cashedUser);
		assertEquals(TestUserFactory.maskEmail(userRequestDto.getEmail()), cashedUser.getMaskedEmail());

		userService.deleteUser(userIdUUID, userId, adminRoles);
		assertNull(cacheManager.getCache("users").get(userIdUUID));
	}

	@Test
	void getCardByUserIdAndCardId_ShouldCacheResult() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto();
		String userId = UUID.randomUUID().toString();
		UserResponseDto userResponseDto = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = userResponseDto.getId();

		CardRequestDto cardRequestDto = TestCardFactory.createCardRequestDto();
		CardResponseDto cardResponseDto = cardService.createCard(userIdUUID, cardRequestDto);

		CardResponseDto firstCall = cardService.getCardByUserIdAndCardId(userIdUUID, cardResponseDto.getId());
		assertNotNull(firstCall);

		CardResponseDto secondCall = cardService.getCardByUserIdAndCardId(userIdUUID, cardResponseDto.getId());
		assertNotNull(secondCall);

		assertNotNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cardResponseDto.getId()));
		assertEquals(firstCall, secondCall);
	}

	@Test
	void createCard_ShouldEvictUserFromCache() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto();
		String userId = UUID.randomUUID().toString();
		UserResponseDto userResponseDto = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = userResponseDto.getId();

		CardRequestDto cardRequestDto = TestCardFactory.createCardRequestDto();
		CardResponseDto savedCard1 = cardService.createCard(userIdUUID, cardRequestDto);

		CardResponseDto cachedCard = cardService.getCardByUserIdAndCardId(userIdUUID, savedCard1.getId());
		assertNotNull(cachedCard);
		assertNotNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cachedCard.getId()));
		assertEquals(cachedCard.getMaskedHolder(), savedCard1.getMaskedHolder());

		CardRequestDto secondCardRequestDto = TestCardFactory.createCardRequestDto();
		secondCardRequestDto.setNumber("1111111111111111");
		CardResponseDto savedCard2 = cardService.createCard(userIdUUID, secondCardRequestDto);
		assertNotNull(savedCard2);
		assertNull(cacheManager.getCache("users").get(userIdUUID));
	}

	@Test
	void updateCard_ShouldEvictCardFromCache() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto();
		String userId = UUID.randomUUID().toString();
		UserResponseDto user = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = user.getId();

		CardRequestDto cardRequestDto = TestCardFactory.createCardRequestDto();
		CardResponseDto createdCard = cardService.createCard(userIdUUID, cardRequestDto);
		UUID cardId = createdCard.getId();

		CardResponseDto firstCall = cardService.getCardByUserIdAndCardId(userIdUUID, cardId);
		assertNotNull(firstCall);
		assertNotNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cardId));

		CardRequestDto updateRequest = TestCardFactory.createCardRequestDto("UPDATED HOLDER", "5555555555555555");
		CardResponseDto updatedCard = cardService.updateCard(userIdUUID, cardId, updateRequest);
		assertNotNull(updatedCard);
		assertNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cardId));
	}

	@Test
	void deleteCard_ShouldEvictCardFromCache() {
		UserRequestDto userRequestDto = TestUserFactory.createUserRequestDto();
		String userId = UUID.randomUUID().toString();
		UserResponseDto user = userService.createUser(userRequestDto, userId);
		UUID userIdUUID = user.getId();

		CardRequestDto cardRequestDto = TestCardFactory.createCardRequestDto();
		CardResponseDto createdCard = cardService.createCard(userIdUUID, cardRequestDto);
		UUID cardId = createdCard.getId();

		CardResponseDto firstCall = cardService.getCardByUserIdAndCardId(userIdUUID, cardId);
		assertNotNull(firstCall);
		assertNotNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cardId));

		cardService.deleteCard(userIdUUID, cardId);
		assertNull(cacheManager.getCache("cards").get(userIdUUID + "_" + cardId));
	}

	@Test
	void getAllUsers_ShouldWorkForAdmin() {
		UserRequestDto userRequestDto1 = TestUserFactory.createUserRequestDto("user1@example.com");
		UserRequestDto userRequestDto2 = TestUserFactory.createUserRequestDto("user2@example.com");

		String userId1 = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();

		userService.createUser(userRequestDto1, userId1);
		userService.createUser(userRequestDto2, userId2);

		List<UserResponseDto> users = userService.getAllUsers(adminRoles);
		assertNotNull(users);
		assertEquals(2, users.size());
	}

	@Test
	void getUsersByIds_ShouldWorkForAdmin() {
		UserRequestDto userRequestDto1 = TestUserFactory.createUserRequestDto("user1@example.com");
		UserRequestDto userRequestDto2 = TestUserFactory.createUserRequestDto("user2@example.com");

		String userId1 = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();

		UserResponseDto user1 = userService.createUser(userRequestDto1, userId1);
		UserResponseDto user2 = userService.createUser(userRequestDto2, userId2);

		List<UUID> userIds = List.of(user1.getId(), user2.getId());
		List<UserResponseDto> users = userService.getUsersByIds(userIds, adminRoles);

		assertNotNull(users);
		assertEquals(2, users.size());
	}
}