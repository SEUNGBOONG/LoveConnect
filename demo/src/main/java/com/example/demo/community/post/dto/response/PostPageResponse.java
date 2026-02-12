// PostPageResponse.java
package com.example.demo.community.post.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponse(
        List<PostResponse> content,
        long totalCount,
        int totalPages,
        int pageNumber,
        int pageSize,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {
    public static PostPageResponse from(Page<PostResponse> page) {
        return new PostPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements(),
                page.isEmpty()
        );
    }
}
