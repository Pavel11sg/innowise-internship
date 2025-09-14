package com.example.tasks.userservice.repository;

import com.example.tasks.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findById(UUID id);

	List<User> findAllByIdIn(List<UUID> ids);

	List<User> findAllById(Iterable<UUID> ids);

	Optional<User> findByEmail(String email);

	void deleteById(UUID id);

	boolean existsByEmail(String email);

	@Query("select u from User u where u.birthDate between :start and :end")
	List<User> findUsersBornBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

	@Modifying(clearAutomatically = true)
	@Query(value = "select * from users where email like '%@' || :domain", nativeQuery = true)
	List<User> findByEmailEndingWith(@Param("domain") String domain);
}
