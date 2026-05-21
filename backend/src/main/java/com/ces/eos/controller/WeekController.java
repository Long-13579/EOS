package com.ces.eos.controller;

import com.ces.eos.dto.response.WeekResponse;
import com.ces.eos.service.WeekService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weeks")
@RequiredArgsConstructor
public class WeekController {

  private final WeekService weekService;

  @GetMapping
  public ResponseEntity<List<WeekResponse>> getLast13Weeks() {
    List<WeekResponse> response = weekService.getLast13Weeks();
    return ResponseEntity.ok(response);
  }
}
