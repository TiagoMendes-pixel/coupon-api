package com.desafioTenda.cupon_api.controller.CouponController;

import java.util.UUID;
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
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                "ACTIVE"
        );

        when(couponService.create(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/coupon")
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
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.description").value("Cupom de teste"))
                .andExpect(jsonPath("$.discountValue").value(10.00))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldFindAllCoupons() throws Exception {

        CouponResponse coupon1 = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom 1",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                "ACTIVE"
        );

        CouponResponse coupon2 = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "DEF456",
                "Cupom 2",
                new BigDecimal("20.00"),
                LocalDateTime.of(2027, 1, 31, 23, 59),
                true,
                false,
                "ACTIVE"
        );

        when(couponService.findAll())
                .thenReturn(List.of(coupon1, coupon2));

        mockMvc.perform(get("/coupon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("ABC123"))
                .andExpect(jsonPath("$[1].code").value("DEF456"));
    }

    @Test
    void shouldFindCouponById() throws Exception {

        CouponResponse coupon = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                "ACTIVE"
        );

        when(couponService.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(coupon);

        mockMvc.perform(get("/coupon/{id}", UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.description").value("Cupom de teste"))
                .andExpect(jsonPath("$.discountValue").value(10.00))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {

        when(couponService.findById(UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .thenThrow(new CouponNotFoundException(UUID.fromString("00000000-0000-0000-0000-000000000999")));

        mockMvc.perform(get("/coupon/{id}", UUID.fromString("00000000-0000-0000-0000-000000000999")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Coupon not found with id: 00000000-0000-0000-0000-000000000999"));
    }

    @Test
    void shouldPublishCoupon() throws Exception {

        CouponResponse publishedCoupon = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true,
                false,
                "ACTIVE"
        );

        when(couponService.publish(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(publishedCoupon);

        mockMvc.perform(patch("/coupon/{id}/publish", UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.redeemed").value(false))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRedeemCoupon() throws Exception {

        CouponResponse redeemedCoupon = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true,
                true,
                "ACTIVE"
        );

        when(couponService.redeem(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(redeemedCoupon);

        mockMvc.perform(patch("/coupon/{id}/redeem", UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.redeemed").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldDeleteCoupon() throws Exception {

        CouponResponse deletedCoupon = new CouponResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ABC123",
                "Cupom de teste",
                new BigDecimal("10.00"),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false,
                false,
                "ACTIVE"
        );

        when(couponService.delete(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(deletedCoupon);

        mockMvc.perform(delete("/coupon/{id}", UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnConflictWhenDeletingAlreadyDeletedCoupon() throws Exception {

        when(couponService.delete(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenThrow(new IllegalStateException(
                        "Coupon has already been deleted"
                ));

        mockMvc.perform(delete("/coupon/{id}", UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .andExpect(status().isConflict())
                .andExpect(content().string(
                        "Coupon has already been deleted"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingInvalidCoupon() throws Exception {

        when(couponService.create(any()))
                .thenThrow(new IllegalArgumentException(
                        "Discount value must be greater than or equal to 0.5"
                ));

        mockMvc.perform(
                        post("/coupon")
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