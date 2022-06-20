package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.batch.utils.Utility;
import com.alok.spring.response.GetSalaryByCompanyResponse;
import com.alok.spring.response.GetTransactionResponse;
import com.alok.spring.response.GetTransactionsResponse;
import com.alok.spring.response.UploadFileResponse;
import com.alok.spring.service.BankService;
import com.alok.spring.service.FileStorageService;
import com.alok.spring.service.JobExecutorOfBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bank")
public class BankStatementController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobExecutorOfBankService bankJobExecutorService;

    @Autowired
    private BankService bankService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @Deprecated
    @LogExecutionTime
    @CrossOrigin
    @PostMapping(value = "/statement/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadFileResponse> uploadStatement(
            @RequestParam MultipartFile file
    ) {

        log.info("Uploaded file: {}, type: {}, size: {}", file.getOriginalFilename(),
                file.getContentType(), file.getSize());

        String fileName = fileStorageService.storeFile(file, Utility.getUploadType(file.getOriginalFilename()));

        try {
            bankJobExecutorService.executeBatchJob(Utility.getUploadType(file.getOriginalFilename()), fileName);
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .body(
                        UploadFileResponse.builder()
                                .fileName( fileName)
                                .size(file.getSize())
                                .fileType(file.getContentType())
                                .message("File submitted for processing")
                                .fileDownloadUri("/report/download")
                                .build()
                );
    }

    @LogExecutionTime
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetTransactionsResponse> getAllTransactions() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getAllTransactions());
    }

    @LogExecutionTime
    @CrossOrigin
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetTransactionResponse> getTransaction(@PathVariable(value = "id") Integer id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(bankService.getTransaction(id));
    }

    @LogExecutionTime
    @GetMapping(value = "/salary/bycompany", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetSalaryByCompanyResponse> getSalaryByCompany() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(bankService.getSalaryByCompany());
    }
}
