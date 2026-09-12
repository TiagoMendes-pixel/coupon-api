package com.desafioTenda.cupon_api.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByDeletedFalse();

    Optional<Coupon> findByIdAndDeletedFalse(Long id);
}
