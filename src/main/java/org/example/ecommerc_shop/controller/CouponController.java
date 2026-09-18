package org.example.ecommerc_shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.dto.ApiResponse;
import org.example.ecommerc_shop.dto.request.CouponCreateRequest;
import org.example.ecommerc_shop.dto.request.CouponUpdateRequest;
import org.example.ecommerc_shop.dto.response.CouponResponse;
import org.example.ecommerc_shop.service.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupons")
@Validated
public class CouponController {

    private final CouponService couponService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CouponResponse> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.createCoupon(request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<Page<CouponResponse>> getAllCoupons(
            @RequestParam(name = "page_size") Integer pageSize,
            @RequestParam(name = "page_number") Integer pageNumber) {

        return ApiResponse.<Page<CouponResponse>>builder()
                .result(couponService.getAllCoupon(pageSize, pageNumber))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{couponId}")
    public ApiResponse<CouponResponse> getCouponById(
            @PathVariable String couponId) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.getCouponById(couponId))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{couponId}")
    public ApiResponse<CouponResponse> updateCoupon(
            @PathVariable String couponId,
            @Valid @RequestBody CouponUpdateRequest request) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.updateCoupon(couponId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{couponId}")
    public ApiResponse<Void> deleteCoupon(
            @PathVariable String couponId) {

        couponService.deleteCoupon(couponId);

        return ApiResponse.<Void>builder()
                .message("Delete Coupon Successfully")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{couponId}/status")
    public ApiResponse<CouponResponse> updateStatus(
            @PathVariable String couponId,
            @RequestParam CouponStatus status) {

        return ApiResponse.<CouponResponse>builder()
                .result(couponService.updateStatus(couponId, status))
                .build();
    }
}
