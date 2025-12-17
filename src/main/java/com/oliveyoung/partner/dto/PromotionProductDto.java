package com.oliveyoung.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromotionProductDto {
    private String prodCd;
    private String prodNm;
    private Long promoPrice;
    private String startDt;
    private String endDt;
}
