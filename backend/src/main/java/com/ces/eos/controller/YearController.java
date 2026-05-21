package com.ces.eos.controller;

import com.ces.eos.dto.response.YearResponse;
import com.ces.eos.service.YearService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/years")
@RequiredArgsConstructor
public class YearController {
  private final YearService yearService;

  @GetMapping
  public ResponseEntity<List<YearResponse>> getYears() {
    return ResponseEntity.ok(yearService.getYears());
  }
}
