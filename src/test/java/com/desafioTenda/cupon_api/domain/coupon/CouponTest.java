package com.desafioTenda.cupon_api.domain.coupon;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CouponTest {

    @Test
    public void shouldCreateCouponWhenDiscountValueIsMinimum() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        assertNotNull(coupon);
        assertEquals("ABC123", coupon.getCode());
    }

    @Test
    public void shouldNotCreateCouponWhenDiscountValueIsBelowMinimum() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        "Coupon description",
                        new BigDecimal("0.49"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldRemoveSpecialCharactersFromCode() {

        Coupon coupon = new Coupon(
                "ABC-123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        assertEquals("ABC123", coupon.getCode());
    }

    @Test
    public void shouldNotCreateCouponWhenCodeHasLessThanSixCharacters() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC12",
                        "Coupon description",
                        new BigDecimal("0.5"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenCodeHasMoreThanSixCharacters() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC1234",
                        "Coupon description",
                        new BigDecimal("0.5"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenExpirationDateIsInThePast() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        "Coupon description",
                        new BigDecimal("0.5"),
                        LocalDateTime.now().minusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenExpirationDateIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        "Coupon description",
                        new BigDecimal("0.5"),
                        null,
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenDescriptionIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        null,
                        new BigDecimal("0.5"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenDescriptionIsEmpty() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        "",
                        new BigDecimal("0.5"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldNotCreateCouponWhenDescriptionContainsOnlySpaces() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Coupon(
                        "ABC123",
                        "   ",
                        new BigDecimal("0.5"),
                        LocalDateTime.now().plusDays(1),
                        false
                )
        );
    }

    @Test
    public void shouldCreateCouponWithCorrectInitialStatus() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        assertFalse(coupon.isPublished());
        assertFalse(coupon.isRedeemed());
        assertFalse(coupon.isDeleted());
    }

    @Test
    public void shouldPublishCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.publish();

        assertTrue(coupon.isPublished());
    }

    @Test
    public void shouldNotRedeemCouponWhenItIsNotPublished() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        assertThrows(
                IllegalStateException.class,
                coupon::redeem
        );
    }

    @Test
    public void shouldRedeemPublishedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.publish();
        coupon.redeem();

        assertTrue(coupon.isRedeemed());
    }

    @Test
    public void shouldNotRedeemCouponTwice() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.publish();
        coupon.redeem();

        assertThrows(
                IllegalStateException.class,
                coupon::redeem
        );
    }

    @Test
    public void shouldNotRedeemExpiredCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusSeconds(1),
                false
        );

        coupon.publish();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        assertThrows(
                IllegalStateException.class,
                coupon::redeem
        );
    }

    @Test
    public void shouldDeleteCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.delete();

        assertTrue(coupon.isDeleted());
    }

    @Test
    public void shouldNotDeleteRedeemedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.publish();
        coupon.redeem();

        assertThrows(
                IllegalStateException.class,
                coupon::delete
        );
    }

    @Test
    public void shouldNotRedeemDeletedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.delete();

        assertThrows(
                IllegalStateException.class,
                coupon::redeem
        );
    }

    @Test
    public void shouldNotPublishDeletedCoupon() {

        Coupon coupon = new Coupon(
                "ABC123",
                "Coupon description",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                false
        );

        coupon.delete();

        assertThrows(
                IllegalStateException.class,
                coupon::publish
        );
    }

}