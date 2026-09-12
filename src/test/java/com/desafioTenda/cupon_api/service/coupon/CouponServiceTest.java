package com.desafioTenda.cupon_api.service.coupon;

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
import java.time.LocalDateTime;
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
                LocalDateTime.now().plusDays(10),
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
                LocalDateTime.now().plusDays(10),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        CouponResponse response = couponService.findById(1L);

        assertNotNull(response);
        assertEquals("ABC123", response.code());
        assertEquals("Cupom de teste", response.description());

        verify(couponRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void shouldThrowExceptionWhenCouponIsNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.findById(999L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(999L);
    }

    @Test
    void shouldFindAllCoupons() {

        Coupon coupon1 = new Coupon(
                "ABC123",
                "Cupom 1",
                new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(10),
                false
        );

        Coupon coupon2 = new Coupon(
                "DEF456",
                "Cupom 2",
                new BigDecimal("20.00"),
                LocalDateTime.now().plusDays(20),
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
                LocalDateTime.now().plusDays(10),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.publish(1L);

        assertTrue(response.published());

        verify(couponRepository).findByIdAndDeletedFalse(1L);
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldRedeemPublishedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(10),
                false
        );

        coupon.publish();

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.redeem(1L);

        assertTrue(response.redeemed());

        verify(couponRepository).findByIdAndDeletedFalse(1L);
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldNotRedeemCouponWhenItIsNotPublished() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(10),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        assertThrows(
                IllegalStateException.class,
                () -> couponService.redeem(1L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(1L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldDeleteCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(10),
                false
        );

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        when(couponRepository.save(coupon))
                .thenReturn(coupon);

        CouponResponse response = couponService.delete(1L);

        assertTrue(response.deleted());

        verify(couponRepository).findByIdAndDeletedFalse(1L);
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldNotDeleteRedeemedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(10),
                false
        );

        coupon.publish();
        coupon.redeem();

        when(couponRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(coupon));

        assertThrows(
                IllegalStateException.class,
                () -> couponService.delete(1L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(1L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenPublishingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.publish(999L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(999L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenRedeemingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.redeem(999L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(999L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingCouponNotFound() {

        when(couponRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                CouponNotFoundException.class,
                () -> couponService.delete(999L)
        );

        verify(couponRepository).findByIdAndDeletedFalse(999L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

}