package com.desafioTenda.cupon_api.dto.coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String description,
        BigDecimal discountValue,
        LocalDateTime expirationDate,
        boolean published,
        boolean redeemed,
        boolean deleted
) {
}