package com.desafioTenda.cupon_api.service.coupon;

import com.desafioTenda.cupon_api.domain.coupon.Coupon;
import com.desafioTenda.cupon_api.domain.coupon.CouponRepository;
import com.desafioTenda.cupon_api.dto.coupon.CouponResponse;
import com.desafioTenda.cupon_api.dto.coupon.CreateCouponRequest;
import com.desafioTenda.cupon_api.exception.CouponNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponResponse create(CreateCouponRequest request) {

        Coupon coupon = new Coupon(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponse(savedCoupon);
    }

    public List<CouponResponse> findAll() {

        return couponRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CouponResponse findById(Long id) {

        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CouponNotFoundException(id));


        return toResponse(coupon);

    }

    public CouponResponse publish(Long id) {

        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.publish();

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponse(savedCoupon);
    }

    public CouponResponse redeem(Long id) {

        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.redeem();

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponse(savedCoupon);
    }

    public CouponResponse delete(Long id) {

        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        coupon.delete();

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponse(savedCoupon);
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.isPublished(),
                coupon.isRedeemed(),
                coupon.isDeleted()
        );
    }
}