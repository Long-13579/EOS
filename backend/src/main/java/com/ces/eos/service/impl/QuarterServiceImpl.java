package com.ces.eos.service.impl;

import com.ces.eos.dto.response.QuarterResponse;
import com.ces.eos.entity.Quarter;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.mapper.QuarterMapper;
import com.ces.eos.repository.QuarterRepository;
import com.ces.eos.service.QuarterService;
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
public class QuarterServiceImpl implements QuarterService {
  private final QuarterRepository quarterRepository;
  private final QuarterMapper quarterMapper;

  @Override
  public List<QuarterResponse> getQuarters() {
    log.info("action=getQuarters.start");
    log.debug("action=getQuarters.repo.findAllByOrderByNameAsc");
    List<QuarterResponse> quarters =
        quarterRepository.findAllByOrderByNameAsc().stream()
            .map(quarterMapper::toQuarterResponse)
            .toList();
    log.info("action=getQuarters.success count={}", quarters.size());
    return quarters;
  }

  @Override
  public void validateQuarterExists(UUID quarterId) {
    log.debug("action=validateQuarterExists.start quarterId={}", quarterId);
    if (!quarterRepository.existsById(quarterId)) {
      log.warn("action=validateQuarterExists.validationFailed quarterId={}", quarterId);
      throw new ResourceNotFoundException(
          Map.of("quarterId", List.of(String.format("Quarter not found with id: %s", quarterId))));
    }
    log.debug("action=validateQuarterExists.success quarterId={}", quarterId);
  }

  @Override
  public Quarter getQuarterById(UUID quarterId) {
    log.debug("action=getQuarterById.start quarterId={}", quarterId);
    log.debug("action=getQuarterById.repo.findById quarterId={}", quarterId);
    Quarter quarter =
        quarterRepository
            .findById(quarterId)
            .orElseThrow(
                () -> {
                  log.warn("action=getQuarterById.validationFailed quarterId={}", quarterId);
                  return new ResourceNotFoundException(
                      Map.of(
                          "quarterId",
                          List.of(String.format("Quarter not found with id: %s", quarterId))));
                });
    log.debug("action=getQuarterById.success quarterId={}", quarterId);
    return quarter;
  }
}
