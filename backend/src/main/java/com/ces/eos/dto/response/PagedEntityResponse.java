package com.ces.eos.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedEntityResponse<T>(List<T> data, PaginationResponse pagination) {
  public static <T> PagedEntityResponse<T> from(Page<T> page) {
    PaginationResponse pagination =
        new PaginationResponse(
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious());

    return new PagedEntityResponse<T>(page.getContent(), pagination);
  }
}
