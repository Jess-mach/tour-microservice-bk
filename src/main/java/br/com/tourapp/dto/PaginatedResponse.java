package br.com.tourapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated response wrapper for lists")
public class PaginatedResponse<T> {

    @Schema(description = "List of items for current page")
    private List<T> content;

    @Schema(description = "Pagination metadata")
    private PageMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Pagination metadata")
    public static class PageMetadata {

        @Schema(description = "Current page number (0-based)", example = "0")
        private int page;

        @Schema(description = "Number of items per page", example = "10")
        private int size;

        @Schema(description = "Total number of items", example = "100")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "10")
        private int totalPages;

        @Schema(description = "Whether this is the first page", example = "true")
        private boolean first;

        @Schema(description = "Whether this is the last page", example = "false")
        private boolean last;

        @Schema(description = "Whether there is a next page", example = "true")
        private boolean hasNext;

        @Schema(description = "Whether there is a previous page", example = "false")
        private boolean hasPrevious;

        @Schema(description = "Number of items in current page", example = "10")
        private int numberOfElements;

        @Schema(description = "Whether the page is empty", example = "false")
        private boolean empty;

        @Schema(description = "Sort information")
        private SortInfo sort;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Sort information")
    public static class SortInfo {

        @Schema(description = "Whether content is sorted", example = "true")
        private boolean sorted;

        @Schema(description = "Whether content is unsorted", example = "false")
        private boolean unsorted;

        @Schema(description = "Whether content is empty", example = "false")
        private boolean empty;
    }

    // Helper method to create from Spring Data Page
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .content(page.getContent())
                .pagination(PageMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .numberOfElements(page.getNumberOfElements())
                        .empty(page.isEmpty())
                        .sort(SortInfo.builder()
                                .sorted(page.getSort().isSorted())
                                .unsorted(page.getSort().isUnsorted())
                                .empty(page.getSort().isEmpty())
                                .build())
                        .build())
                .build();
    }

    // Helper method with custom content transformation
    public static <T, R> PaginatedResponse<R> from(Page<T> page, List<R> transformedContent) {
        return PaginatedResponse.<R>builder()
                .content(transformedContent)
                .pagination(PageMetadata.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .numberOfElements(page.getNumberOfElements())
                        .empty(page.isEmpty())
                        .sort(SortInfo.builder()
                                .sorted(page.getSort().isSorted())
                                .unsorted(page.getSort().isUnsorted())
                                .empty(page.getSort().isEmpty())
                                .build())
                        .build())
                .build();
    }
}