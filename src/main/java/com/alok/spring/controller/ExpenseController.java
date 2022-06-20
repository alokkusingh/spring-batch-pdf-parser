package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.response.*;
import com.alok.spring.service.JobExecutorOfExpenseService;
import com.alok.spring.service.ExpenseService;
import com.alok.spring.service.FileStorageService;
import com.alok.spring.constant.UploadType;
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

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/expense")
public class ExpenseController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobExecutorOfExpenseService expenseJobExecutorService;

    @Autowired
    private ExpenseService expenseService;

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

        String fineName = fileStorageService.storeFile(file, UploadType.ExpenseGoogleSheet);

        // brute force way
        try {
            expenseJobExecutorService.executeAllJobs(true);
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
                                .uploadType(UploadType.ExpenseGoogleSheet)
                                .build()
                );
    }

    @LogExecutionTime
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetExpensesResponse> getAllExpenses() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getAllExpenses());
    }

    @LogExecutionTime
    @GetMapping(value = "/current_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetExpensesResponse> getCurrentMonthExpenses() {
        LocalDate currentDate = LocalDate.now();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getCurrentMonthExpenses(currentDate));
    }

    @LogExecutionTime
    @GetMapping(value = "/sum_by_category_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetExpensesMonthSumByCategoryResponse> getMonthWiseExpenseCategorySum() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthWiseExpenseCategorySum());
    }

    @LogExecutionTime
    @GetMapping(value = "/sum_by_month", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetExpensesMonthSumResponse> getMonthWiseExpenseSum() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getMonthWiseExpenseSum());
    }

    @LogExecutionTime
    @GetMapping(value = "/current_month_by_day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetExpensesResponseAggByDay> getCurrentMonthExpensesSumByDay() {
        LocalDate currentDate = LocalDate.now();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(expenseService.getCurrentMonthExpensesSumByDay(currentDate));
    }
}
