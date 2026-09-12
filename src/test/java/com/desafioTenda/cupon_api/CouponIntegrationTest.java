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
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponIntegrationTest {
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired MockMvc mvc;
    @Autowired CouponRepository repository;

    @Test
    void shouldNormalizeOffsetToUtcAndPreserveItInDatabase() throws Exception {
        Instant expiration = Instant.now().plusSeconds(172800).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        String offsetDate = expiration.atOffset(java.time.ZoneOffset.ofHours(-3)).toString();
        mvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"UTC123","description":"UTC test","discountValue":0.5,
                 "expirationDate":"%s","published":true}
                """.formatted(offsetDate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expirationDate").value(expiration.toString()));
        repository.flush();
        entityManager.clear();
        Coupon saved = repository.findByDeletedFalse().stream()
                .filter(c -> c.getCode().equals("UTC123")).findFirst().orElseThrow();
        assertEquals(expiration, saved.getExpirationDate());
        mvc.perform(get("/coupon/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value(expiration.toString()));
    }

    @Test
    void shouldRejectPastInstantEvenWhenOffsetLooksLikeFutureLocalTime() throws Exception {
        String past = Instant.now().minusSeconds(3600).atOffset(java.time.ZoneOffset.ofHours(14)).toString();
        mvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"OLD123","description":"Past test","discountValue":0.5,
                 "expirationDate":"%s","published":false}
                """.formatted(past)))
                .andExpect(status().isBadRequest());
        assertTrue(repository.findByDeletedFalse().stream().noneMatch(c -> c.getCode().equals("OLD123")));
    }

    @Test
    void shouldRejectDateWithoutTimeZone() throws Exception {
        mvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"BAD123","description":"No zone","discountValue":0.5,
                 "expirationDate":"2027-12-31T23:59:59","published":false}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateSanitizedCouponWithUuidAndStatus() throws Exception {
        mvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"ABC-123","description":"Integration test","discountValue":0.5,
                 "expirationDate":"%s","published":true}
                """.formatted(Instant.now().plusSeconds(172800))))
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
                Instant.now().plusSeconds(172800), true);
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
