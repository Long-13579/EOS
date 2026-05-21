package com.ces.eos.service;

import com.ces.eos.dto.response.IssueTypeBaseResponse;
import com.ces.eos.entity.IssueType;
import java.util.List;
import java.util.UUID;

public interface IssueTypeService {
  List<IssueTypeBaseResponse> getIssueTypes();

  IssueType getIssueTypeById(UUID issueTypeId);

  IssueType getIssueTypeByName(String name);
}
