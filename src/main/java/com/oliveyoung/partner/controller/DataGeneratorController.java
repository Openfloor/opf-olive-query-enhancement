package com.oliveyoung.partner.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "10000") int productCount,
            @RequestParam(defaultValue = "50000") int promotionCount) {

        // 기존 데이터 삭제
        jdbcTemplate.execute("DELETE FROM TB_PROMOTION");
        jdbcTemplate.execute("DELETE FROM TB_PRODUCT");

        // 상품 데이터 생성
        for (int i = 1; i <= productCount; i++) {
            String useYn = (i % 10 == 0) ? "N" : "Y";  // 10%는 미사용
            jdbcTemplate.update(
                "INSERT INTO TB_PRODUCT (PROD_CD, PROD_NM, USE_YN) VALUES (?, ?, ?)",
                String.format("P%06d", i),
                "테스트상품" + i,
                useYn
            );
        }

        // 프로모션 데이터 생성
        for (int i = 1; i <= promotionCount; i++) {
            String prodCd = String.format("P%06d", (i % productCount) + 1);
            String startDt, endDt;

            if (i % 3 == 0) {
                // 현재 진행중인 프로모션
                startDt = "20251201";
                endDt = "20251231";
            } else if (i % 3 == 1) {
                // 지난 프로모션
                startDt = "20251101";
                endDt = "20251130";
            } else {
                // 미래 프로모션
                startDt = "20260101";
                endDt = "20260131";
            }

            jdbcTemplate.update(
                "INSERT INTO TB_PROMOTION (PROD_CD, PROMO_PRICE, START_DT, END_DT) VALUES (?, ?, ?, ?)",
                prodCd,
                10000 + (i * 100),
                startDt,
                endDt
            );
        }

        return ResponseEntity.ok(Map.of(
            "message", "Test data generated",
            "products", productCount,
            "promotions", promotionCount
        ));
    }
}
