package com.ces.eos.service;

import com.ces.eos.dto.request.CreateIssueRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateIssueRequest;
import com.ces.eos.dto.response.IssueResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface IssueService {
  IssueResponse addIssue(CreateIssueRequest request, UUID creatorId);

  PagedEntityResponse<IssueResponse> getIssuesByTeam(
      UUID teamId, PaginationRequest request, String issueTypeId, Boolean isArchived);

  void deleteIssueById(UUID issueId);

  IssueResponse updateIssue(UUID issueId, UpdateIssueRequest request);

  IssueResponse updateIssueArchiveStatus(UUID issueId, Boolean isArchived);

  IssueResponse updateIssueType(UUID issueId, UUID issueTypeId);
}
