package com.oliveyoung.partner.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "TB_PROMOTION", indexes = {
    @Index(name = "IDX_PROMO_DATE", columnList = "START_DT, END_DT"),
    @Index(name = "IDX_PROMO_PROD", columnList = "PROD_CD")
})
@Getter
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PROD_CD")
    private String prodCd;

    @Column(name = "PROMO_PRICE")
    private Long promoPrice;

    @Column(name = "START_DT")
    private String startDt;

    @Column(name = "END_DT")
    private String endDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROD_CD", insertable = false, updatable = false)
    private Product product;
}
