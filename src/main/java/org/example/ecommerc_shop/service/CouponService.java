package org.example.ecommerc_shop.service;

import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.dto.request.CouponCreateRequest;
import org.example.ecommerc_shop.dto.request.CouponUpdateRequest;
import org.example.ecommerc_shop.dto.response.CouponResponse;
import org.springframework.data.domain.Page;

public interface CouponService {
    CouponResponse createCoupon(CouponCreateRequest request);
    Page<CouponResponse> getAllCoupon(Integer pageSize, Integer pageNumber);
    void deleteCoupon(String id);
    CouponResponse updateCoupon(String id, CouponUpdateRequest request);
    CouponResponse getCouponById(String id);
    CouponResponse updateStatus(String id, CouponStatus status);
}
