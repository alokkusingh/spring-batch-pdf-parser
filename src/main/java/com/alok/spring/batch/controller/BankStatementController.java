package com.alok.spring.batch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/bank/statement")
public class BankStatementController {


    @PostMapping("/upload")
    public static ResponseEntity<String> uploadStatement(
            @RequestParam MultipartFile file,
            @RequestParam String name
    ) {

        log.info("Uploaded file: {}", file.getOriginalFilename());
        log.info("File type: {}", file.getContentType());
        log.info("File size: {}", file.getSize());



        return ResponseEntity.ok()
                .body("File uploaded with job id: " + "tbd");
    }
}
