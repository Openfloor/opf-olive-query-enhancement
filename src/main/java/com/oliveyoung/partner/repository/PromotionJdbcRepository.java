package com.oliveyoung.partner.repository;

import com.oliveyoung.partner.dto.PromotionProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PromotionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * [AS-IS] 레거시 쿼리 - 성능 비교용
     */
    public List<PromotionProductDto> findByLegacyQuery() {
        String sql = """
            SELECT *
            FROM (
                SELECT A.PROD_CD, A.PROD_NM, B.PROMO_PRICE, B.START_DT, B.END_DT
                FROM TB_PRODUCT A, TB_PROMOTION B
                WHERE A.PROD_CD = B.PROD_CD
                AND A.USE_YN = 'Y'
            )
            WHERE FORMATDATETIME(CURRENT_DATE, 'yyyyMMdd') BETWEEN START_DT AND END_DT
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new PromotionProductDto(
                rs.getString("PROD_CD"),
                rs.getString("PROD_NM"),
                rs.getLong("PROMO_PRICE"),
                rs.getString("START_DT"),
                rs.getString("END_DT")
            )
        );
    }

    /**
     * [TO-BE] 개선된 쿼리 - Native SQL 버전
     */
    public List<PromotionProductDto> findByOptimizedQuery(String currentDate) {
        String sql = """
            SELECT A.PROD_CD, A.PROD_NM, B.PROMO_PRICE, B.START_DT, B.END_DT
            FROM TB_PRODUCT A
            INNER JOIN TB_PROMOTION B ON A.PROD_CD = B.PROD_CD
            WHERE A.USE_YN = 'Y'
            AND B.START_DT <= ?
            AND B.END_DT >= ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new PromotionProductDto(
                rs.getString("PROD_CD"),
                rs.getString("PROD_NM"),
                rs.getLong("PROMO_PRICE"),
                rs.getString("START_DT"),
                rs.getString("END_DT")
            ),
            currentDate, currentDate
        );
    }
}
