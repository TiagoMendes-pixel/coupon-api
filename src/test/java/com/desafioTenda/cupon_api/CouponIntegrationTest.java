package com.desafioTenda.cupon_api;

import com.desafioTenda.cupon_api.domain.coupon.Coupon;
import com.desafioTenda.cupon_api.domain.coupon.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CouponRepository repository;

    @Test
    void shouldCreateSanitizedCouponWithUuidAndStatus() throws Exception {
        mvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"ABC-123","description":"Integration test","discountValue":0.5,
                 "expirationDate":"%s","published":true}
                """.formatted(LocalDateTime.now().plusDays(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.deleted").doesNotExist());
        Coupon saved = repository.findByDeletedFalse().stream()
                .filter(c -> c.getDescription().equals("Integration test")).findFirst().orElseThrow();
        assertNotNull(saved.getId());
        assertEquals("ABC123", saved.getCode());
        mvc.perform(get("/coupon/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldSoftDeleteRedeemedCouponAndRejectRepeat() throws Exception {
        Coupon coupon = new Coupon("DEL123", "Keep this information", new BigDecimal("1.5"),
                LocalDateTime.now().plusDays(2), true);
        coupon.redeem();
        repository.saveAndFlush(coupon);
        mvc.perform(delete("/coupon/{id}", coupon.getId()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        repository.flush();
        Coupon saved = repository.findById(coupon.getId()).orElseThrow();
        assertTrue(saved.isDeleted());
        assertTrue(saved.isRedeemed());
        assertEquals("Keep this information", saved.getDescription());
        mvc.perform(get("/coupon/{id}", coupon.getId())).andExpect(status().isNotFound());
        mvc.perform(delete("/coupon/{id}", coupon.getId())).andExpect(status().isNotFound());
    }
}
