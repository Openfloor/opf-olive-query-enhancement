package com.oliveyoung.partner.service;

import com.oliveyoung.partner.dto.PromotionProductDto;
import com.oliveyoung.partner.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<PromotionProductDto> getActivePromotions() {
        String today = LocalDate.now().format(DATE_FORMAT);
        return promotionRepository.findActivePromotions(today);
    }
}
