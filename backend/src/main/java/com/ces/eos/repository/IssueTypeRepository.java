package com.ces.eos.repository;

import com.ces.eos.entity.IssueType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueTypeRepository extends JpaRepository<IssueType, UUID> {

  List<IssueType> findAllByOrderByNameAsc();

  Optional<IssueType> findByName(String name);
}
