package org.example.ecommerc_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryUploadResponse implements Serializable {

    private String url;

    private String publicId;
}