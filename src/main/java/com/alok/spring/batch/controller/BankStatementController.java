package com.alok.spring.batch.controller;

import com.alok.spring.batch.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FileStorageService fileStorageService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadStatement(
            @RequestParam MultipartFile file
    ) {

        log.info("Uploaded file: {}, type: {}, size: {}", file.getOriginalFilename(),
                file.getContentType(), file.getSize());

        fileStorageService.storeFile(file);

        return ResponseEntity.ok()
                .body("File uploaded with job id: " + "tbd");
    }
}
