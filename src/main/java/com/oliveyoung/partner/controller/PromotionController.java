package com.oliveyoung.partner.controller;

import com.oliveyoung.partner.dto.PromotionProductDto;
import com.oliveyoung.partner.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionProductDto>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }
}
