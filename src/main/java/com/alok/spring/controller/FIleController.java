package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.batch.utils.Utility;
import com.alok.spring.constant.UploadType;
import com.alok.spring.response.UploadFileResponse;
import com.alok.spring.service.FileStorageService;
import com.alok.spring.service.JobExecutorOfBankService;
import com.alok.spring.service.JobExecutorOfExpenseService;
import com.alok.spring.service.JobExecutorOfTaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
public class FIleController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobExecutorOfExpenseService expenseJobExecutorService;

    @Autowired
    private JobExecutorOfTaxService taxJobExecutorService;

    @Autowired
    private JobExecutorOfBankService bankJobExecutorService;

    @Value("${web.cache-control.max-age}")
    private Long cacheControlMaxAge;

    @LogExecutionTime
    @CrossOrigin
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadFileResponse> uploadStatement(
            @RequestParam MultipartFile file
    ) {

        log.info("Uploaded file: {}, type: {}, size: {}", file.getOriginalFilename(),
                file.getContentType(), file.getSize());

        UploadType uploadType = Utility.getUploadType(file.getOriginalFilename());

        String fineName = fileStorageService.storeFile(file, uploadType);

        // brute force way
        try {
            if (uploadType == UploadType.ExpenseGoogleSheet)
                expenseJobExecutorService.executeAllJobs(true);

            if (uploadType == UploadType.TaxGoogleSheet)
                taxJobExecutorService.executeAllJobs(true);

            if (uploadType == UploadType.HDFCExportedStatement || uploadType == UploadType.KotakExportedStatement)
                bankJobExecutorService.executeBatchJob(uploadType, file.getOriginalFilename());

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
                                .fileName(fineName)
                                .size(file.getSize())
                                .fileType(file.getContentType())
                                .message("File submitted for processing")
                                .uploadType(uploadType)
                                .build()
                );
    }
}
