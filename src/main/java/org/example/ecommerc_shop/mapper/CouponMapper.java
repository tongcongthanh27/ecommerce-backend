package org.example.ecommerc_shop.mapper;

import org.example.ecommerc_shop.dto.request.CouponCreateRequest;
import org.example.ecommerc_shop.dto.request.CouponUpdateRequest;
import org.example.ecommerc_shop.dto.response.CouponResponse;
import org.example.ecommerc_shop.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponse toCouponResponse(Coupon coupon);
    Coupon toCoupon(CouponCreateRequest couponCreateRequest);

    //@MappingTarget có ý nghĩa là:
    //Không tạo Coupon mới, mà lấy Coupon đang tồn tại rồi cập nhật các field từ CouponUpdateRequest vào nó.
    void updateCoupon(
            CouponUpdateRequest request,
            @MappingTarget Coupon coupon
    );


}
