package org.example.ecommerc_shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.common.CouponStatus;
import org.example.ecommerc_shop.common.DiscountType;
import org.example.ecommerc_shop.dto.request.CouponCreateRequest;
import org.example.ecommerc_shop.dto.request.CouponUpdateRequest;
import org.example.ecommerc_shop.dto.response.CouponResponse;
import org.example.ecommerc_shop.entity.Coupon;
import org.example.ecommerc_shop.exception.AppException;
import org.example.ecommerc_shop.exception.ErrorCode;
import org.example.ecommerc_shop.mapper.CouponMapper;
import org.example.ecommerc_shop.repository.CouponRepository;
import org.example.ecommerc_shop.service.CouponService;
import org.example.ecommerc_shop.service.specification.CouponSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        if (couponRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new AppException(ErrorCode.COUPON_ALREADY_EXISTS);
        }
        Coupon coupon = couponMapper.toCoupon(request);
        coupon.setDeleted(false);
        couponRepository.save(coupon);
        return couponMapper.toCouponResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupon(Integer pageSize, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Coupon> couponPage = couponRepository.findAllByDeletedFalse(pageable);
        return couponPage.map(couponMapper::toCouponResponse);
    }

    @Override
    @Transactional
    public void deleteCoupon(String id) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPONNOTFOUND));
        coupon.setDeleted(true);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(String id, CouponUpdateRequest request) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPONNOTFOUND));
        couponMapper.updateCoupon(request, coupon);
        return couponMapper.toCouponResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(String id) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPONNOTFOUND));
        return couponMapper.toCouponResponse(coupon);
    }

    @Override
    @Transactional
    public CouponResponse updateStatus(String id, CouponStatus status) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPONNOTFOUND));
        coupon.setStatus(status);
        return couponMapper.toCouponResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> filter(
            String code,
            CouponStatus status,
            DiscountType discountType,
            LocalDateTime startDateFrom,
            LocalDateTime startDateTo,
            LocalDateTime endDateFrom,
            LocalDateTime endDateTo,
            int page,
            int size
    ) {
        Specification<Coupon> specification = CouponSpec.isNotDeleted();
        if (code != null && !code.isEmpty()) {
            specification = specification.and(CouponSpec.likeCode(code));
        }
        if (status != null) {
            specification = specification.and(CouponSpec.hasStatus(status));
        }
        if (discountType != null) {
            specification = specification.and(CouponSpec.hasDiscountType(discountType));
        }
        if (startDateFrom != null) {
            specification = specification.and(CouponSpec.startDateGreaterThanOrEqual(startDateFrom));
        }
        if (startDateTo != null) {
            specification = specification.and(CouponSpec.startDateLessThanOrEqual(startDateTo));
        }
        if (endDateFrom != null) {
            specification = specification.and(CouponSpec.endDateGreaterThanOrEqual(endDateFrom));
        }
        if (endDateTo != null) {
            specification = specification.and(CouponSpec.endDateLessThanOrEqual(endDateTo));
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return couponRepository.findAll(specification, pageable).map(couponMapper::toCouponResponse);
    }
}
