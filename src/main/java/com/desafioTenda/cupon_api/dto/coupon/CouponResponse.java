package com.desafioTenda.cupon_api.dto.coupon;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        Instant expirationDate,
        boolean published,
        boolean redeemed,
        String status
) {
}