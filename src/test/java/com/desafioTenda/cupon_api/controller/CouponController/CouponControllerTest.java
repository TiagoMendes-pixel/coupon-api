package com.desafioTenda.cupon_api.controller.CouponController;

import com.desafioTenda.cupon_api.dto.coupon.CouponResponse;
import com.desafioTenda.cupon_api.exception.CouponNotFoundException;
import com.desafioTenda.cupon_api.service.coupon.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Test
    void shouldCreateCouponAndReturnCreated() throws Exception {

        CouponResponse response = new CouponResponse(
                1L,
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                false
        );

        when(couponService.create(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "code": "ABC123",
                                          "description": "Cupom de teste",
                                          "discountValue": 10.00,
                                          "expirationDate": "2026-12-31T23:59:00",
                                          "published": false
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.description").value("Cupom de teste"))
                .andExpect(jsonPath("$.discountValue").value(10.00))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void shouldFindAllCoupons() throws Exception {

        CouponResponse coupon1 = new CouponResponse(
                1L,
                "ABC123",
                "Cupom 1",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                false
        );

        CouponResponse coupon2 = new CouponResponse(
                2L,
                "DEF456",
                "Cupom 2",
                new BigDecimal("20.00"),
                LocalDateTime.of(2027, 1, 31, 23, 59),
                true,
                false,
                false
        );

        when(couponService.findAll())
                .thenReturn(List.of(coupon1, coupon2));

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("ABC123"))
                .andExpect(jsonPath("$[1].code").value("DEF456"));
    }

    @Test
    void shouldFindCouponById() throws Exception {

        CouponResponse coupon = new CouponResponse(
                1L,
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                false
        );

        when(couponService.findById(1L))
                .thenReturn(coupon);

        mockMvc.perform(get("/coupons/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.description").value("Cupom de teste"))
                .andExpect(jsonPath("$.discountValue").value(10.00))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {

        when(couponService.findById(999L))
                .thenThrow(new CouponNotFoundException(999L));

        mockMvc.perform(get("/coupons/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Coupon not found with id: 999"));
    }

    @Test
    void shouldPublishCoupon() throws Exception {

        CouponResponse publishedCoupon = new CouponResponse(
                1L,
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true,
                false,
                false
        );

        when(couponService.publish(1L))
                .thenReturn(publishedCoupon);

        mockMvc.perform(patch("/coupons/{id}/publish", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void shouldRedeemCoupon() throws Exception {

        CouponResponse redeemedCoupon = new CouponResponse(
                1L,
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true,
                true,
                false
        );

        when(couponService.redeem(1L))
                .thenReturn(redeemedCoupon);

        mockMvc.perform(patch("/coupons/{id}/redeem", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.redeemed").value(true))
                .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void shouldDeleteCoupon() throws Exception {

        CouponResponse deletedCoupon = new CouponResponse(
                1L,
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                true
        );

        when(couponService.delete(1L))
                .thenReturn(deletedCoupon);

        mockMvc.perform(delete("/coupons/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void shouldReturnConflictWhenDeletingRedeemedCoupon() throws Exception {

        when(couponService.delete(1L))
                .thenThrow(new IllegalStateException(
                        "Redeemed coupon cannot be deleted"
                ));

        mockMvc.perform(delete("/coupons/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(content().string(
                        "Redeemed coupon cannot be deleted"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidCoupon() throws Exception {

        when(couponService.create(any()))
                .thenThrow(new IllegalArgumentException(
                        "Discount value must be greater than or equal to 0.5"
                ));

        mockMvc.perform(
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "code": "ABC123",
                                      "description": "Cupom inválido",
                                      "discountValue": 0.20,
                                      "expirationDate": "2026-12-31T23:59:00",
                                      "published": false
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Discount value must be greater than or equal to 0.5"
                ));
    }
}