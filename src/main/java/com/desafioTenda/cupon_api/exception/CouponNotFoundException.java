package com.desafioTenda.cupon_api.exception;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(UUID id) {
        super("Coupon not found with id: " + id);
    }
}