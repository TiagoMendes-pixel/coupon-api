package com.desafioTenda.cupon_api.dto.coupon;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        LocalDateTime expirationDate,
        boolean published,
        boolean redeemed,
        String status
) {
}