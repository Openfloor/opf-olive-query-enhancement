package com.oliveyoung.partner.repository;

import com.oliveyoung.partner.dto.PromotionProductDto;
import com.oliveyoung.partner.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * [TO-BE] 개선된 프로모션 상품 조회 쿼리
     *
     * 개선 포인트:
     * 1. 불필요한 서브쿼리 제거 -> 단일 쿼리로 통합
     * 2. 구식 콤마 조인 -> ANSI 표준 INNER JOIN 사용
     * 3. TO_CHAR(SYSDATE) 반복 호출 제거 -> 파라미터로 전달
     * 4. SELECT * 제거 -> 필요한 컬럼만 명시적 조회
     * 5. 인덱스 활용: (START_DT, END_DT), (PROD_CD) 복합 인덱스 권장
     */
    @Query("SELECT new com.oliveyoung.partner.dto.PromotionProductDto(" +
           "p.prodCd, pr.prodNm, p.promoPrice, p.startDt, p.endDt) " +
           "FROM Promotion p " +
           "INNER JOIN p.product pr " +
           "WHERE pr.useYn = 'Y' " +
           "AND p.startDt <= :currentDate " +
           "AND p.endDt >= :currentDate")
    List<PromotionProductDto> findActivePromotions(@Param("currentDate") String currentDate);
}
