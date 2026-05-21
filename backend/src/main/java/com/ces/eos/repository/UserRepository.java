package com.ces.eos.repository;

import com.ces.eos.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  @Query(
      value = "SELECT u FROM User u JOIN FETCH u.role",
      countQuery = "SELECT count(u) FROM User u")
  Page<User> findAll(Pageable pageable);

  @Query(
      value =
          "SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName))"
              + " LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',"
              + " :search, '%'))",
      countQuery =
          "SELECT count(u) FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE"
              + " LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',"
              + " :search, '%'))")
  Page<User> searchUsers(@Param("search") String search, Pageable pageable);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :id AND u.isActive = true")
  Optional<User> findActiveUser(@Param("id") UUID id);

  @Query("SELECT u FROM User u JOIN u.teams t WHERE t.id = :teamId")
  List<User> findUsersByTeamId(@Param("teamId") UUID teamId, Sort sort);

  @Query("SELECT u FROM User u JOIN u.teams t WHERE u.id = :userId AND t.id = :teamId")
  Optional<User> findByIdAndTeamId(@Param("userId") UUID userId, @Param("teamId") UUID teamId);

  @Query(
      "SELECT u FROM User u JOIN u.teams t WHERE u.id IN :userIds AND t.id = :teamId AND u.isActive"
          + " = true")
  Set<User> findAllByIdInAndTeamIdAndIsActiveTrue(
      @Param("userIds") Set<UUID> userIds, @Param("teamId") UUID teamId);

  boolean existsByIdAndTeams_Id(UUID userId, UUID teamId);
}
