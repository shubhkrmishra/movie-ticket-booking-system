package com.sk.movie.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promoCodeId;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    private Integer maxUses;

    @Column(nullable = false)
    private Integer currentUses = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getPromoCodeId() { return promoCodeId; }
    public void setPromoCodeId(Long promoCodeId) { this.promoCodeId = promoCodeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }

    public Integer getCurrentUses() { return currentUses; }
    public void setCurrentUses(Integer currentUses) { this.currentUses = currentUses; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
