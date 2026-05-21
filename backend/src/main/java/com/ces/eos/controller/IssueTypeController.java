package com.ces.eos.controller;

import com.ces.eos.dto.response.IssueTypeBaseResponse;
import com.ces.eos.service.IssueTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/issue-types")
@RequiredArgsConstructor
public class IssueTypeController {

  private final IssueTypeService issueTypeService;

  @GetMapping
  public ResponseEntity<List<IssueTypeBaseResponse>> getIssueTypes() {
    return ResponseEntity.ok(issueTypeService.getIssueTypes());
  }
}
