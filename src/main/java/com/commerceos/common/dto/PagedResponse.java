package com.commerceos.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic wrapper for paginated API responses.
 *
 * @param <T> the type of data items in the current page
 */
public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  /** Build a {@code PagedResponse} from a Spring Data {@link Page}. */
  public static <T> PagedResponse<T> from(Page<?> page, List<T> content) {
    return new PagedResponse<>(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
