package org.example.ecommerc_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse implements Serializable {
    private String id;

    private String name;

    private String description;

    private String parentId;

    private String imageUrl;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();
}
