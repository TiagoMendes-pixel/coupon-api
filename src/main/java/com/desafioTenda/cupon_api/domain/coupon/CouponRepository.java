package com.desafioTenda.cupon_api.domain.coupon;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    List<Coupon> findByDeletedFalse();

    Optional<Coupon> findByIdAndDeletedFalse(UUID id);
}
