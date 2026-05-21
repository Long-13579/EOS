package com.ces.eos.service;

import com.ces.eos.dto.request.CreateRockRequest;
import com.ces.eos.dto.request.UpdateRockRequest;
import com.ces.eos.dto.response.RockListResponse;
import com.ces.eos.dto.response.RockResponse;
import com.ces.eos.dto.response.UserRockListResponse;
import com.ces.eos.entity.Rock;
import com.ces.eos.enums.RockStatus;
import java.util.UUID;

public interface RockService {

  RockListResponse getRocksByTeam(UUID teamId, UUID yearId, UUID quarterId, Boolean isArchived);

  RockResponse addRock(CreateRockRequest request, UUID creatorId);

  RockResponse updateRockArchiveStatus(UUID rockId, Boolean isArchived);

  Rock getRockById(UUID rockId);

  RockResponse updateRockStatus(UUID rockId, RockStatus newStatus);

  RockResponse updateRock(UUID rockId, UpdateRockRequest request, UUID userId);

  UserRockListResponse findActiveRocksByOwnerId(UUID ownerId, UUID yearId, UUID quarterId);
}
