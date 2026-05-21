package com.ces.eos.service.impl;

import com.ces.eos.dto.response.YearResponse;
import com.ces.eos.entity.CustomYear;
import com.ces.eos.exception.ResourceNotFoundException;
import com.ces.eos.exception.ServerInternalException;
import com.ces.eos.mapper.YearMapper;
import com.ces.eos.repository.YearRepository;
import com.ces.eos.service.YearService;
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
public class YearServiceImpl implements YearService {

  private final YearRepository yearRepository;
  private final YearMapper yearMapper;

  @Override
  public List<YearResponse> getYears() {
    log.info("action=getYears.start");
    log.debug("action=getYears.repo.findAllByOrderByYearAsc");
    List<YearResponse> years =
        yearRepository.findAllByOrderByYearAsc().stream().map(yearMapper::toYearResponse).toList();
    log.info("action=getYears.success count={}", years.size());
    return years;
  }

  @Override
  public void validateYearExists(UUID yearId) {
    log.debug("action=validateYearExists.start yearId={}", yearId);
    log.debug("action=validateYearExists.repo.existsById yearId={}", yearId);
    if (!yearRepository.existsById(yearId)) {
      log.warn("action=validateYearExists.validationFailed yearId={}", yearId);
      throw new ResourceNotFoundException(
          Map.of("yearId", List.of(String.format("Year not found with id: %s", yearId))));
    }
    log.debug("action=validateYearExists.success yearId={}", yearId);
  }

  @Override
  @Transactional
  public CustomYear getOrCreateYear(Integer yearValue) {
    log.debug("action=getOrCreateYear.start year={}", yearValue);
    log.debug("action=getOrCreateYear.repo.insertIfNotExists year={}", yearValue);
    yearRepository.insertIfNotExists(yearValue);
    log.debug("action=getOrCreateYear.repo.findByYear year={}", yearValue);
    CustomYear year =
        yearRepository
            .findByYear(yearValue)
            .orElseThrow(
                () ->
                    new ServerInternalException(
                        String.format(
                            "Year upsert produced no row for year %d — possible trigger rejection"
                                + " or schema issue.",
                            yearValue)));
    log.debug("action=getOrCreateYear.success yearId={}", year.getId());
    return year;
  }
}
