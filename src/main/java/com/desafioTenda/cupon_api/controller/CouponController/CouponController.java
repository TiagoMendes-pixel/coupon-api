package com.desafioTenda.cupon_api.controller.CouponController;

import java.util.UUID;
import com.desafioTenda.cupon_api.dto.coupon.CouponResponse;
import com.desafioTenda.cupon_api.dto.coupon.CreateCouponRequest;
import com.desafioTenda.cupon_api.service.coupon.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(
            @RequestBody CreateCouponRequest request
    ) {

        CouponResponse coupon = couponService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(coupon);
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> findAll() {
        return ResponseEntity.ok(couponService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.findById(id));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<CouponResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.publish(id));
    }

    @PatchMapping("/{id}/redeem")
    public ResponseEntity<CouponResponse> redeem(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.redeem(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
