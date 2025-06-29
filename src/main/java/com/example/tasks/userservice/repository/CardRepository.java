package com.example.tasks.userservice.repository;

import com.example.tasks.userservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
	Optional<Card> findById(Long id);

	List<Card> findAllById(Iterable<Long> ids);

	List<Card> findByUserId(Long userId);

	void deleteById(Long id);

	@Query("select c from Card c where c.user.id = :userId and c.expiration_date > current_date")
	List<Card> findValidCardsByUserId(@Param("userId") Long userId);

	@Modifying
	@Query(value = "update card_info set holder = :holder where user_id = :userId", nativeQuery = true)
	int updateCardHolderByUser(@Param("userId") Long userId, @Param("holder") String holder);
}
