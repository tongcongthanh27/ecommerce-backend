package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResponse uploadImage(MultipartFile file);

    void deleteImage(String publicId);
}