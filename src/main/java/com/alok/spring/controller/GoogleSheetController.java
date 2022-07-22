package com.alok.spring.controller;

import com.alok.spring.service.GoogleSheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/gsheet")
public class GoogleSheetController {

    private GoogleSheetService googleSheetService;

    public GoogleSheetController(GoogleSheetService googleSheetService) {
        this.googleSheetService = googleSheetService;
    }

    @GetMapping("/refresh/tax")
    public ResponseEntity<String> refreshTaxData() {

        CompletableFuture.runAsync(() -> {
            try {
                googleSheetService.refreshTaxData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.accepted()
                .body("Refresh submitted");
    }

    @GetMapping("/refresh/expense")
    public ResponseEntity<String> refreshExpenseData() throws IOException {

        CompletableFuture.runAsync(() -> {
            try {
                googleSheetService.refreshExpenseData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.accepted()
                .body("Refresh submitted");
    }

    @GetMapping("/refresh/investment")
    public ResponseEntity<String> refreshInvestmentData() {

        CompletableFuture.runAsync(() -> {
            try {
                googleSheetService.refreshInvestmentData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return ResponseEntity.accepted()
                .body("Refresh submitted");
    }
}