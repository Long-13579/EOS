package com.ces.eos.dto.response;

import java.util.UUID;

public record QuarterBaseResponse(UUID id, String name, String startDate, String endDate) {}
