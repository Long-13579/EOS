package com.ces.eos.controller;

import com.ces.eos.dto.response.QuarterResponse;
import com.ces.eos.service.QuarterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quarters")
@RequiredArgsConstructor
public class QuarterController {
  private final QuarterService quarterService;

  @GetMapping
  public ResponseEntity<List<QuarterResponse>> getQuarters() {
    return ResponseEntity.ok(quarterService.getQuarters());
  }
}
