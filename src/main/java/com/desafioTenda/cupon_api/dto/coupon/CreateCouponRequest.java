package com.desafioTenda.cupon_api.dto.coupon;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(String code,
                                  String description,
                                  BigDecimal discountValue,
                                  Instant expirationDate,
                                  boolean published) {
}
