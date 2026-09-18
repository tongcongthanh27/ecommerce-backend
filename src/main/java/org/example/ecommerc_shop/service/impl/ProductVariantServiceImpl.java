package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.StockStatus;
import org.example.ecommerc_shop.dto.request.ProductVariantCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductVariantUpdateRequest;
import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.example.ecommerc_shop.entity.Product;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.ProductVariantMapper;
import org.example.ecommerc_shop.repository.ProductRepository;
import org.example.ecommerc_shop.repository.ProductVariantRepository;
import org.example.ecommerc_shop.service.CloudinaryService;
import org.example.ecommerc_shop.service.ProductVariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductVariantResponse createVariant(String id, ProductVariantCreateRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (productVariantRepository.existsProductVariantByVariantNameAndDeletedFalse(request.getVariantName())) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_EXISTED);
        }
        if (productVariantRepository.existsProductVariantBySkuAndDeletedFalse(request.getSku())) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_EXISTED);
        }
        ProductVariant productVariant = productVariantMapper.toVariant(request);
        if (request.getImage() != null
                && !request.getImage().isEmpty()) {

            CloudinaryUploadResponse uploadResult =
                    cloudinaryService.uploadImage(request.getImage());

            productVariant.setImageUrl(uploadResult.getUrl());
            productVariant.setImagePublicId(uploadResult.getPublicId());
        }
        productVariant.setProduct(product);
        productVariantRepository.save(productVariant);
        ProductVariantResponse response = productVariantMapper.toVariantResponse(productVariant);
        if (request.getQuantityInStock() > 0) {
            response.setStockStatus(String.valueOf(StockStatus.IN_STOCK));
        } else {
            response.setStockStatus(String.valueOf(StockStatus.OUT_OF_STOCK));
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteVariant(String id) {
        ProductVariant productVariant = productVariantRepository.findProductVariantByIdAndDeletedFalse(id);
        if (productVariant == null) {
            throw new AppException(ErrorCode.VARIANTNOTFOUND);
        }
        productVariant.setDeleted(true);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(String variantId, ProductVariantUpdateRequest request) {
        ProductVariant productVariant = productVariantRepository.findProductVariantByIdAndDeletedFalse(variantId);
        if (productVariant == null) {
            throw new AppException(ErrorCode.VARIANTNOTFOUND);
        }
        if (request.getVariantName() != null && productVariantRepository
                .existsProductVariantByVariantNameAndIdNotAndDeletedFalse(request.getVariantName(), variantId)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_EXISTED);
        }
        if (request.getSku() != null && productVariantRepository
                .existsProductVariantBySkuAndIdNotAndDeletedFalse(request.getSku(), variantId)) {
            throw new AppException(ErrorCode.SKU_ALREADY_EXISTS);
        }
        productVariantMapper.updateVariant(request, productVariant);
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            CloudinaryUploadResponse uploadResult = cloudinaryService.uploadImage(request.getImage());
            productVariant.setImageUrl(uploadResult.getUrl());
            productVariant.setImagePublicId(uploadResult.getPublicId());
        }
        productVariantRepository.save(productVariant);
        ProductVariantResponse response =
                productVariantMapper.toVariantResponse(productVariant);
        if (productVariant.getQuantityInStock() > 0) {
            response.setStockStatus(String.valueOf(StockStatus.IN_STOCK));
        } else {
            response.setStockStatus(String.valueOf(StockStatus.OUT_OF_STOCK));
        }
        return response;
    }

    @Override
    public Page<ProductVariantResponse> getAllVariant(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize);
        Page<ProductVariant> productVariantPage = productVariantRepository.findAllByDeletedFalse(pageable);
        return productVariantPage.map(productVariantMapper::toVariantResponse);
    }
}
