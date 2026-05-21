package com.ces.eos.constant;

import com.ces.eos.dto.common.SortField;
import java.util.Arrays;
import org.springframework.data.domain.Sort;

public class SortingConstants {
  public static final Sort DEFAULT_ENTITIES_SORT =
      Sort.by(
          Sort.Order.desc("createdAt"), // get recent users first
          Sort.Order.asc("id") // not duplicated
          );

  private SortingConstants() {}

  public static Sort customEntitiesSort(SortField... sortFields) {
    if (sortFields == null || sortFields.length == 0) {
      return DEFAULT_ENTITIES_SORT;
    }

    Sort sort =
        Sort.by(
            Arrays.stream(sortFields).map(p -> new Sort.Order(p.direction(), p.field())).toList());

    return sort.and(DEFAULT_ENTITIES_SORT);
  }
}
