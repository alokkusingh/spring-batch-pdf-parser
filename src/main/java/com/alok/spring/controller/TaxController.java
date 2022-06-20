package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.response.GetTaxesResponse;
import com.alok.spring.service.FileStorageService;
import com.alok.spring.service.TaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/tax")
public class TaxController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TaxService taxService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetTaxesResponse> getAllTaxes() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(taxService.getAllTaxes());
    }
}
