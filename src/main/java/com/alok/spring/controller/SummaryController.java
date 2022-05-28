package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.response.GetMonthlySummaryResponse;
import com.alok.spring.service.SummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @LogExecutionTime
    @GetMapping(value = "/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetMonthlySummaryResponse> getMonthlySummary() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(summaryService.getMonthSummary());
    }
}
