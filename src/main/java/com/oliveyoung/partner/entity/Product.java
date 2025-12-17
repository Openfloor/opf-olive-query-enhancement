package com.oliveyoung.partner.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "TB_PRODUCT")
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @Column(name = "PROD_CD")
    private String prodCd;

    @Column(name = "PROD_NM")
    private String prodNm;

    @Column(name = "USE_YN")
    private String useYn;
}
