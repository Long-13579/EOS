package com.ces.eos.service;

import com.ces.eos.dto.request.CreateHeadlineRequest;
import com.ces.eos.dto.request.PaginationRequest;
import com.ces.eos.dto.request.UpdateHeadlineRequest;
import com.ces.eos.dto.response.HeadlineResponse;
import com.ces.eos.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface HeadlineService {
  HeadlineResponse createHeadline(CreateHeadlineRequest request, UUID creatorId);

  PagedEntityResponse<HeadlineResponse> getHeadlinesByTeam(
      UUID teamId, PaginationRequest request, Boolean isArchived);

  HeadlineResponse updateHeadline(UUID headlineId, UpdateHeadlineRequest request, UUID updaterId);

  HeadlineResponse updateHeadlineArchiveStatus(UUID headlineId, Boolean isArchived);

  void deleteHeadline(UUID headlineId);
}
