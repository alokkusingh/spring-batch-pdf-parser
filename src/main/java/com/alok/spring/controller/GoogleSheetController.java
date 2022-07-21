package com.alok.spring.controller;

import com.alok.spring.service.GoogleSheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/gsheet")
public class GoogleSheetController {

    private GoogleSheetService googleSheetService;

    public GoogleSheetController(GoogleSheetService googleSheetService) {
        this.googleSheetService = googleSheetService;
    }

    @GetMapping("/refresh/tax")
    public ResponseEntity<String> refreshTaxData() throws IOException {

        googleSheetService.refreshTaxData();

        return ResponseEntity.ok()
                .body("Refreshed");
    }

    @GetMapping("/refresh/expense")
    public ResponseEntity<String> refreshExpenseData() throws IOException {

        googleSheetService.refreshExpenseData();

        return ResponseEntity.ok()
                .body("Refreshed");
    }

    @GetMapping("/refresh/investment")
    public ResponseEntity<String> refreshInvestmentData() throws IOException {

        googleSheetService.refreshInvestmentData();

        return ResponseEntity.ok()
                .body("Refreshed");
    }
}