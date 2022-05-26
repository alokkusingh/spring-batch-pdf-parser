package com.alok.spring.batch.controller;

import com.alok.spring.batch.response.GetMonthlySummaryResponse;
import com.alok.spring.batch.service.SummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/summary")
public class SummaryController {

    private SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping(value = "/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetMonthlySummaryResponse> getMonthlySummary() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(300, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(summaryService.getMonthSummary());
    }
}