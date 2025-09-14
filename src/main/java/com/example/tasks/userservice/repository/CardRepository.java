package com.example.tasks.userservice.repository;

import com.example.tasks.userservice.model.Card;
import com.example.tasks.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
	Optional<Card> findById(UUID id);

	List<Card> findAllById(Iterable<UUID> ids);

	List<Card> findAllByUserId(UUID userId);

	@Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.id = :cardId")
	Optional<Card> findByUserIdAndId(@Param("userId") UUID userId, @Param("cardId") UUID cardId);

	boolean existsByUserAndNumber(User user, String number);

	void deleteById(UUID id);

	@Query("select c from Card c where c.user.id = :userId and c.expirationDate > current_date")
	List<Card> findValidCardsByUserId(@Param("userId") UUID userId);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE Card c SET c.holder = :holder WHERE c.user.id = :userId")
	int updateCardHolderByUser(@Param("userId") UUID userId, @Param("holder") String holder);
}
