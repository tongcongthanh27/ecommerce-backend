package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.dto.request.ProductCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductUpdateRequest;
import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.example.ecommerc_shop.dto.response.ProductDetailResponse;
import org.example.ecommerc_shop.dto.response.ProductResponse;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.example.ecommerc_shop.entity.Category;
import org.example.ecommerc_shop.entity.Product;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.ProductMapper;
import org.example.ecommerc_shop.mapper.ProductVariantMapper;
import org.example.ecommerc_shop.repository.CategoryRepository;
import org.example.ecommerc_shop.repository.ProductRepository;
import org.example.ecommerc_shop.repository.ProductVariantRepository;
import org.example.ecommerc_shop.service.CategoryService;
import org.example.ecommerc_shop.service.CloudinaryService;
import org.example.ecommerc_shop.service.ProductService;
import org.example.ecommerc_shop.service.ProductVariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findByIdAndDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (productRepository.existsByNameAndDeletedFalse(request.getName())){
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        if (request.getImage() != null
                && !request.getImage().isEmpty()) {

            CloudinaryUploadResponse uploadResult =
                    cloudinaryService.uploadImage(request.getImage());

            product.setThumbnailUrl(uploadResult.getUrl());
            product.setThumbnailPublicId(uploadResult.getPublicId());
        }
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setDeleted(true);
    }

    @Override
    @Transactional
    public Page<ProductResponse> getAllProduct(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize);
        Page<Product> productPage = productRepository.findAllByDeletedFalse(pageable);
        return productPage.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional
    public ProductDetailResponse getProductById(String id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        List<ProductVariant> productVariantList =
                productVariantRepository
                        .findAllByProduct_IdAndDeletedFalse(id);

        ProductDetailResponse response =
                productMapper.toProductDetailResponse(product);

        List<ProductVariantResponse> variantResponses =
                productVariantList.stream()
                        .map(productVariantMapper::toVariantResponse)
                        .toList();

        response.setVariants(variantResponses);

        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            Category category = categoryRepository
                    .findByIdAndDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            if (!product.getName().equals(request.getName()) && productRepository.existsByNameAndDeletedFalseAndIdNot(request.getName(), id)) {
                throw new AppException(ErrorCode.PRODUCT_EXISTED);
            }
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (product.getThumbnailPublicId() != null) {
                cloudinaryService.deleteImage(
                        product.getThumbnailPublicId()
                );
            }
            CloudinaryUploadResponse uploadResult = cloudinaryService.uploadImage(request.getImage());
            product.setThumbnailUrl(uploadResult.getUrl());
            product.setThumbnailPublicId(uploadResult.getPublicId());
        }
        return productMapper.toProductResponse(product);
    }
}
