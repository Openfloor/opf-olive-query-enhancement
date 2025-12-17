package com.oliveyoung.partner.controller;

import com.oliveyoung.partner.dto.PromotionProductDto;
import com.oliveyoung.partner.repository.PromotionJdbcRepository;
import com.oliveyoung.partner.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final PromotionJdbcRepository jdbcRepository;
    private final PromotionRepository jpaRepository;

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareBenchmark(
            @RequestParam(defaultValue = "100") int iterations) {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Map<String, Object> result = new LinkedHashMap<>();

        // Warm-up
        for (int i = 0; i < 10; i++) {
            jdbcRepository.findByLegacyQuery();
            jdbcRepository.findByOptimizedQuery(today);
        }

        // AS-IS 측정
        long[] legacyTimes = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            jdbcRepository.findByLegacyQuery();
            legacyTimes[i] = System.nanoTime() - start;
        }

        // TO-BE 측정
        long[] optimizedTimes = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            jdbcRepository.findByOptimizedQuery(today);
            optimizedTimes[i] = System.nanoTime() - start;
        }

        // 결과 계산
        Map<String, Object> legacyStats = calculateStats(legacyTimes);
        Map<String, Object> optimizedStats = calculateStats(optimizedTimes);

        double improvement = ((double) legacyStats.get("avg") - (double) optimizedStats.get("avg"))
                            / (double) legacyStats.get("avg") * 100;

        result.put("iterations", iterations);
        result.put("AS-IS (Legacy)", legacyStats);
        result.put("TO-BE (Optimized)", optimizedStats);
        result.put("improvement_percent", String.format("%.2f%%", improvement));

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> calculateStats(long[] times) {
        Arrays.sort(times);
        long sum = 0;
        for (long t : times) sum += t;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("avg", sum / times.length / 1000.0);  // microseconds
        stats.put("min", times[0] / 1000.0);
        stats.put("max", times[times.length - 1] / 1000.0);
        stats.put("p50", times[times.length / 2] / 1000.0);
        stats.put("p95", times[(int)(times.length * 0.95)] / 1000.0);
        stats.put("unit", "microseconds (μs)");
        return stats;
    }
}
