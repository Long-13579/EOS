package com.ces.eos.service.impl;

import com.ces.eos.dto.response.IssueTypeBaseResponse;
import com.ces.eos.entity.IssueType;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.IssueTypeMapper;
import com.ces.eos.repository.IssueTypeRepository;
import com.ces.eos.service.IssueTypeService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueTypeServiceImpl implements IssueTypeService {

  private final IssueTypeMapper issueTypeMapper;
  private final IssueTypeRepository issueTypeRepository;

  @Override
  public List<IssueTypeBaseResponse> getIssueTypes() {
    log.info("action=getIssueTypes.start");
    log.debug("action=getIssueTypes.repo.findAllByOrderByNameAsc");
    List<IssueTypeBaseResponse> issueTypes =
        issueTypeRepository.findAllByOrderByNameAsc().stream()
            .map(issueTypeMapper::toIssueTypeBaseResponse)
            .toList();
    log.info("action=getIssueTypes.success count={}", issueTypes.size());
    return issueTypes;
  }

  @Override
  public IssueType getIssueTypeById(UUID issueTypeId) {
    log.debug("action=getIssueTypeById.start issueTypeId={}", issueTypeId);
    log.debug("action=getIssueTypeById.repo.findById issueTypeId={}", issueTypeId);
    IssueType issueType =
        issueTypeRepository
            .findById(issueTypeId)
            .orElseThrow(
                () -> {
                  log.warn("action=getIssueTypeById.validationFailed issueTypeId={}", issueTypeId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "issueTypeId",
                          List.of(String.format("Issue type not found with id: %s", issueTypeId))));
                });
    log.debug("action=getIssueTypeById.success issueTypeId={}", issueTypeId);
    return issueType;
  }

  @Override
  public IssueType getIssueTypeByName(String name) {
    log.debug("action=getIssueTypeByName.start");
    log.debug("action=getIssueTypeByName.repo.findByName");
    IssueType issueType =
        issueTypeRepository
            .findByName(name)
            .orElseThrow(
                () -> {
                  log.warn("action=getIssueTypeByName.validationFailed name={}", name);
                  return new IllegalStateException(
                      String.format(
                          "Critical configuration error: Issue Type '%s' not found in database.",
                          name));
                });
    log.debug("action=getIssueTypeByName.success issueTypeId={}", issueType.getId());
    return issueType;
  }
}
