package com.ces.eos.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRockListResponse(List<RockResponse> data) {}
