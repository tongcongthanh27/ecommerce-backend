package org.example.ecommerc_shop.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.StockStatus;
import org.example.ecommerc_shop.dto.request.ProductVariantCreateRequest;
import org.example.ecommerc_shop.dto.request.ProductVariantUpdateRequest;
import org.example.ecommerc_shop.dto.response.CloudinaryUploadResponse;
import org.example.ecommerc_shop.dto.response.ProductVariantResponse;
import org.example.ecommerc_shop.dto.response.ProductVariantExcelExport;
import org.example.ecommerc_shop.entity.Product;
import org.example.ecommerc_shop.entity.ProductVariant;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.ProductVariantMapper;
import org.example.ecommerc_shop.repository.ProductRepository;
import org.example.ecommerc_shop.repository.ProductVariantRepository;
import org.example.ecommerc_shop.service.CloudinaryService;
import org.example.ecommerc_shop.service.ProductVariantService;
import org.example.ecommerc_shop.service.specification.ProductVariantSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private static final int EXPORT_BATCH_SIZE = 500;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductVariantResponse createVariant(String id, ProductVariantCreateRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (productVariantRepository.existsByProduct_IdAndVariantNameAndDeletedFalse(
                id, request.getVariantName().trim())) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NAME_ALREADY_EXISTS);
        }
        String normalizedSku = request.getSku().trim().toUpperCase(Locale.ROOT);
        if (productVariantRepository.existsProductVariantBySkuAndDeletedFalse(normalizedSku)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NAME_ALREADY_EXISTS);
        }
        ProductVariant productVariant = productVariantMapper.toVariant(request);
        productVariant.setVariantName(request.getVariantName().trim());
        productVariant.setSku(normalizedSku);
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
        response.setStockStatus(stockStatus(productVariant));
        return response;
    }

    @Override
    @Transactional
    public void deleteVariant(String id) {
        ProductVariant productVariant = productVariantRepository.findProductVariantByIdAndDeletedFalse(id);
        if (productVariant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }
        productVariant.setDeleted(true);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(String variantId, ProductVariantUpdateRequest request) {
        ProductVariant productVariant = productVariantRepository.findProductVariantByIdAndDeletedFalse(variantId);
        if (productVariant == null) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }
        if (request.getVariantName() != null && !request.getVariantName().isBlank() && productVariantRepository
                .existsByProduct_IdAndVariantNameAndDeletedFalseAndIdNot(
                        productVariant.getProduct().getId(), request.getVariantName().trim(), variantId)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_NAME_ALREADY_EXISTS);
        }
        if (request.getVariantName() != null && request.getVariantName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PRODUCT_VARIANT);
        }
        if (request.getSku() != null && request.getSku().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PRODUCT_VARIANT);
        }
        String normalizedSku = request.getSku() == null ? null : request.getSku().trim().toUpperCase(Locale.ROOT);
        if (normalizedSku != null && productVariantRepository
                .existsProductVariantBySkuAndIdNotAndDeletedFalse(normalizedSku, variantId)) {
            throw new AppException(ErrorCode.PRODUCT_VARIANT_SKU_ALREADY_EXISTS);
        }
        productVariantMapper.updateVariant(request, productVariant);
        if (request.getVariantName() != null) productVariant.setVariantName(request.getVariantName().trim());
        if (normalizedSku != null) productVariant.setSku(normalizedSku);
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String oldPublicId = productVariant.getImagePublicId();
            CloudinaryUploadResponse uploadResult = cloudinaryService.uploadImage(request.getImage());
            productVariant.setImageUrl(uploadResult.getUrl());
            productVariant.setImagePublicId(uploadResult.getPublicId());
            if (oldPublicId != null && !oldPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldPublicId);
            }
        }
        productVariantRepository.save(productVariant);
        ProductVariantResponse response =
                productVariantMapper.toVariantResponse(productVariant);
        response.setStockStatus(stockStatus(productVariant));
        return response;
    }

    private String stockStatus(ProductVariant variant) {
        return String.valueOf(variant.getQuantityInStock() != null && variant.getQuantityInStock() > 0
                ? StockStatus.IN_STOCK : StockStatus.OUT_OF_STOCK);
    }

    @Override
    public Page<ProductVariantResponse> getAllVariant(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber -1 , pageSize);
        Page<ProductVariant> productVariantPage = productVariantRepository.findAllByDeletedFalse(pageable);
        return productVariantPage.map(variant -> {
            ProductVariantResponse response = productVariantMapper.toVariantResponse(variant);
            response.setStockStatus(stockStatus(variant));
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductVariantResponse> filter(
            String variantName,
            String sku,
            String productId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        Specification<ProductVariant> specification = ProductVariantSpec.isNotDeleted();
        if (variantName != null && !variantName.isEmpty()) {
            specification = specification.and(ProductVariantSpec.likeVariantName(variantName));
        }
        if (sku != null && !sku.isEmpty()) {
            specification = specification.and(ProductVariantSpec.likeSku(sku));
        }
        if (productId != null && !productId.isEmpty()) {
            specification = specification.and(ProductVariantSpec.hasProductId(productId));
        }
        if (minPrice != null) {
            specification = specification.and(ProductVariantSpec.priceGreaterThanOrEqual(minPrice));
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_RANGE);
        }
        if (maxPrice != null) {
            specification = specification.and(ProductVariantSpec.priceLessThanOrEqual(maxPrice));
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return productVariantRepository.findAll(specification, pageable)
                .map(variant -> {
                    ProductVariantResponse response = productVariantMapper.toVariantResponse(variant);
                    response.setStockStatus(stockStatus(variant));
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public void exportVariants(OutputStream outputStream) {
        try (ExcelWriter writer = EasyExcel.write(outputStream, ProductVariantExcelExport.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("Product Variants").build();
            int pageNo = 0;
            while (true) {
                Pageable pageable = PageRequest.of(pageNo, EXPORT_BATCH_SIZE, Sort.by(
                        Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
                Slice<ProductVariant> slice = productVariantRepository.findForExport(pageable);
                if (slice.isEmpty()) {
                    break;
                }
                List<ProductVariantExcelExport> batch = slice.getContent().stream()
                        .map(productVariantMapper::toExcelExport)
                        .toList();
                writer.write(batch, sheet);
                if (!slice.hasNext()) {
                    break;
                }
                pageNo++;
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public org.example.ecommerc_shop.dto.response.ProductVariantStatisticsResponse getStatistics() {
        var row = productVariantRepository.getStatistics(5);
        if (row == null) {
            return org.example.ecommerc_shop.dto.response.ProductVariantStatisticsResponse.builder()
                    .totalVariants(0).inStockVariants(0).lowStockVariants(0)
                    .outOfStockVariants(0).totalQuantityInStock(0).averagePrice(BigDecimal.ZERO).build();
        }
        return org.example.ecommerc_shop.dto.response.ProductVariantStatisticsResponse.builder()
                .totalVariants(value(row.getTotalVariants()))
                .inStockVariants(value(row.getInStockVariants()))
                .lowStockVariants(value(row.getLowStockVariants()))
                .outOfStockVariants(value(row.getOutOfStockVariants()))
                .totalQuantityInStock(value(row.getTotalQuantityInStock()))
                .averagePrice(row.getAveragePrice() == null ? BigDecimal.ZERO : row.getAveragePrice())
                .build();
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }

}
