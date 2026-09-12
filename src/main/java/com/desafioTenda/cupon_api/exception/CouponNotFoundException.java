package com.desafioTenda.cupon_api.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(Long id) {
        super("Coupon not found with id: " + id);
    }
}