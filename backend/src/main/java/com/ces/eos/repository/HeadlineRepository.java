package com.ces.eos.repository;

import com.ces.eos.entity.Headline;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadlineRepository extends JpaRepository<Headline, UUID> {
  @Query(
      value =
          "SELECT h.id FROM Headline h "
              + "WHERE h.team.id = :teamId AND h.isArchived = :isArchived")
  Page<UUID> findHeadlineIdsByTeamId(
      @Param("teamId") UUID teamId, @Param("isArchived") Boolean isArchived, Pageable pageable);

  @Query(
      value =
          "SELECT DISTINCT h FROM Headline h "
              + "JOIN FETCH h.team "
              + "LEFT JOIN FETCH h.createdBy "
              + "LEFT JOIN FETCH h.updatedBy "
              + "WHERE h.id IN :ids")
  List<Headline> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      """
              SELECT h FROM Headline h
              LEFT JOIN FETCH h.createdBy
              LEFT JOIN FETCH h.updatedBy
              LEFT JOIN FETCH h.team
              WHERE h.id = :id
      """)
  Optional<Headline> findById(@Param("id") UUID id);

  boolean existsByIdAndTeam_Users_Id(UUID headlineId, UUID userId);
}
