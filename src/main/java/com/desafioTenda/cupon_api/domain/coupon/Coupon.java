package com.desafioTenda.cupon_api.domain.coupon;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Coupon {

    private static final BigDecimal MIN_DISCOUNT_VALUE = new BigDecimal("0.5");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private LocalDateTime expirationDate;
    private boolean published;
    private boolean redeemed;
    private boolean deleted;

    protected Coupon() {
        // Construtor utilizado pelo JPA
    }

    public Coupon(String code, String description, BigDecimal discountValue,
                  LocalDateTime expirationDate, boolean published) {

        this.code = sanitizeAndValidateCode(code);
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.redeemed = false;
        this.deleted = false;

        validateDiscountValue();
        validateExpirationDate();
        validateDescription();
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public String getCode() {
        return code;
    }



    public boolean isPublished() {
        return published;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void publish() {
        if (deleted) {
            throw new IllegalStateException(
                    "Deleted coupon cannot be published"
            );
        }
        this.published = true;
    }

    public void redeem() {

        if (deleted) {
            throw new IllegalStateException(
                    "Deleted coupon cannot be redeemed"
            );
        }
        if (!published) {
            throw new IllegalStateException(
                    "Coupon must be published before redemption"
            );
        }

        if (redeemed) {
            throw new IllegalStateException(
                    "Coupon has already been redeemed"
            );
        }

        if (expirationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Coupon has expired"
            );
        }

        this.redeemed = true;
    }

    private void validateDiscountValue() {
        if (discountValue == null || discountValue.compareTo(MIN_DISCOUNT_VALUE) < 0) {
            throw new IllegalArgumentException(
                    "Discount value must be greater than or equal to 0.5"
            );
        }
    }

    private void validateExpirationDate() {
        if (expirationDate == null || expirationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Expiration date must be in the future"
            );
        }
    }

    private String sanitizeAndValidateCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code is required");
        }

        String sanitizedCode = code.replaceAll("[^a-zA-Z0-9]", "");

        if (sanitizedCode.length() != 6) {
            throw new IllegalArgumentException(
                    "Code must contain 6 alphanumeric characters"
            );
        }

        return sanitizedCode;
    }

    private void validateDescription() {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
    }

    public void delete() {
        if (deleted) {
            throw new IllegalStateException(
                    "Coupon has already been deleted"
            );
        }

        this.deleted = true;
    }
}