package com.desafioTenda.cupon_api.service.coupon;

import java.util.UUID;
import com.desafioTenda.cupon_api.domain.coupon.Coupon;
import com.desafioTenda.cupon_api.domain.coupon.CouponRepository;
import com.desafioTenda.cupon_api.dto.coupon.CouponResponse;
import com.desafioTenda.cupon_api.dto.coupon.CreateCouponRequest;
import com.desafioTenda.cupon_api.exception.CouponNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() {

        CreateCouponRequest request = new CreateCouponRequest(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        Coupon savedCoupon = new Coupon(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        when(couponRepository.save(any(Coupon.class)))
                .thenReturn(savedCoupon);

        CouponResponse response = couponService.create(request);

        assertNotNull(response);
        assertEquals("ABC123", response.code());
        assertEquals(new BigDecimal("10.00"), response.discountValue());

        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void shouldFindCouponById() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        CouponResponse response = couponService.findById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertNotNull(response);
        assertEquals("ABC123", response.code());
        assertEquals("Cupom de teste", response.description());

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void shouldThrowExceptionWhenCouponIsNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.findById(UUID.fromString("00000000-0000-0000-0000-000000000999"))
        );

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999"));
    }

    @Test
    void shouldFindAllCoupons() {

        Coupon coupon1 = new Coupon(
                "ABC123",
                "Cupom 1",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        Coupon coupon2 = new Coupon(
                "DEF456",
                "Cupom 2",
                new BigDecimal("20.00"),
                Instant.now().plusSeconds(1728000),
                true
        );

        when(couponRepository.findByDeletedFalse())
                .thenReturn(List.of(coupon1, coupon2));

        List<CouponResponse> response = couponService.findAll();

        assertEquals(2, response.size());
        assertEquals("ABC123", response.get(0).code());
        assertEquals("DEF456", response.get(1).code());

        verify(couponRepository).findByDeletedFalse();
    }

    @Test
    void shouldPublishCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.publish(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertTrue(response.published());

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldRedeemPublishedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        coupon.publish();

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.redeem(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertTrue(response.redeemed());

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldNotRedeemCouponWhenItIsNotPublished() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        assertThrows(
                IllegalStateException.class,
                () -> couponService.redeem(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        );

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldDeleteCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.delete(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertEquals("DELETED", response.status());

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldDeleteRedeemedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                Instant.now().plusSeconds(864000),
                false
        );

        coupon.publish();
        coupon.redeem();

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon)).thenReturn(coupon);
        couponService.delete(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertTrue(coupon.isDeleted());

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldThrowExceptionWhenPublishingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.publish(UUID.fromString("00000000-0000-0000-0000-000000000999"))
        );

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999"));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenRedeemingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.redeem(UUID.fromString("00000000-0000-0000-0000-000000000999"))
        );

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999"));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.delete(UUID.fromString("00000000-0000-0000-0000-000000000999"))
        );

        verify(couponRepository).findByIdAndDeletedFalse(UUID.fromString("00000000-0000-0000-0000-000000000999"));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

}