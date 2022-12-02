package com.alok.spring.service;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.batch.constant.JobConstants;
import com.alok.spring.config.CacheConfig;
import com.alok.spring.constant.Bank;
import com.alok.spring.constant.BatchOf;
import com.alok.spring.constant.MDCKey;
import com.alok.spring.exception.UploadTypeNotSupportedException;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.constant.UploadType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class JobExecutorOfBankService {

    private final ResourceLoader resourceLoader;
    private final String hdfcExportDir;
    private final String kotakExportDir;
    private final JobLauncher jobLauncher;
    private final Job missingAccountJob;
    private final Job citiBankJob1;
    private final Job citiBankJob2;
    private final Job citiBankJob3;
    private final Job kotakBankJob;
    private final Job kotakBankNoPwdJob;
    private final Job kotakImportedAccountJob;
    private final Job kotakImportedAccountJobV2;
    private final Job hdfcImportedAccountJob;
    private final MultiResourceItemReader<Transaction> hdfcImportedItemsReader;
    private final MultiResourceItemReader<Transaction> kotakImportedItemsReaderV2;
    private final CacheService cacheService;
    private final FlatFileItemWriter<Transaction> csvWriterForGoogleSheet;
    private final TransactionRepository transactionRepository;
    private final String outputFileName;

    @Autowired
    public JobExecutorOfBankService(
            JobLauncher jobLauncher,
            ResourceLoader resourceLoader,
            @Value("${dir.path.hdfc_account.imported}")String hdfcExportDir,
            @Value("${dir.path.kotak_account.imported}") String kotakExportDir,
            @Value("${file.export.google.sheet}") String outputFileName,
            @Qualifier("MissingAccountJob") Job missingAccountJob,
            @Qualifier("CitiBankJob1") Job citiBankJob1,
            @Qualifier("CitiBankJob2") Job citiBankJob2,
            @Qualifier("CitiBankJob3") Job citiBankJob3,
            @Qualifier("KotakBankJob") Job kotakBankJob,
            @Qualifier("KotakBankNoPwdJob") Job kotakBankNoPwdJob,
            @Qualifier("KotakImportedAccountJob") Job kotakImportedAccountJob,
            @Qualifier("KotakImportedAccountJobV2") Job kotakImportedAccountJobV2,
            @Qualifier("HDFCImportedAccountJob") Job hdfcImportedAccountJob,
            MultiResourceItemReader<Transaction> hdfcImportedItemsReader,
            MultiResourceItemReader<Transaction> kotakImportedItemsReaderV2,
            CacheService cacheService,
            FlatFileItemWriter<Transaction> csvWriterForGoogleSheet,
            TransactionRepository transactionRepository

    ) {
        this.resourceLoader = resourceLoader;
        this.hdfcExportDir = hdfcExportDir;
        this.kotakExportDir = kotakExportDir;
        this.jobLauncher = jobLauncher;
        this.missingAccountJob = missingAccountJob;
        this.citiBankJob1 = citiBankJob1;
        this.citiBankJob2 = citiBankJob2;
        this.citiBankJob3 = citiBankJob3;
        this.kotakBankJob = kotakBankJob;
        this.kotakBankNoPwdJob = kotakBankNoPwdJob;
        this.kotakImportedAccountJob = kotakImportedAccountJob;
        this.kotakImportedAccountJobV2 = kotakImportedAccountJobV2;
        this.hdfcImportedAccountJob = hdfcImportedAccountJob;
        this.hdfcImportedItemsReader = hdfcImportedItemsReader;
        this.kotakImportedItemsReaderV2 = kotakImportedItemsReaderV2;
        this.cacheService = cacheService;
        this.csvWriterForGoogleSheet = csvWriterForGoogleSheet;
        this.transactionRepository = transactionRepository;
        // outputFileName was required injection via constructor otherwise it was coming null
        // during csvWriterForGoogleSheet bean creation
        this.outputFileName = outputFileName;
    }

    public void executeBatchJob(@NotNull UploadType uploadType, final String fileName) throws Exception {

        log.info("Starting a Bank job execution");

        // Only for below jobs file upload supported - others jobs will be executed at startup only
        // Job - Kotak Exported V2
        switch(uploadType) {
            case KotakExportedStatement -> {
                MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
                kotakImportedItemsReaderV2.setResources(new Resource[]{
                        resourceLoader.getResource("file:" + kotakExportDir + "/" + fileName)
                });
                jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                        .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                        .addString("batchOf", BatchOf.KOTAK_BANK.name())
                        .toJobParameters());
                MDC.remove(MDCKey.BANK.name());
            }
            case HDFCExportedStatement -> {
                MDC.put(MDCKey.BANK.name(), Bank.HDFC.name());
                hdfcImportedItemsReader.setResources(new Resource[]{
                        resourceLoader.getResource("file:" + hdfcExportDir + "/" + fileName)
                });
                jobLauncher.run(hdfcImportedAccountJob, new JobParametersBuilder()
                        .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                        .addString(JobConstants.BATCH_OF, BatchOf.HDFC_BANK.name())
                        .toJobParameters());
                MDC.remove(MDCKey.BANK.name());
            }
            default -> throw new UploadTypeNotSupportedException(String.format("Upload Type %s not supported", uploadType.name()));
        }

        log.debug("Completed job execution");

        cacheService.evictCacheByName(CacheConfig.CacheName.TRANSACTION);
        cacheService.evictCacheByName(CacheConfig.CacheName.SUMMARY);

        generateCSVFile();
    }

    @LogExecutionTime
    public void executeAllBatchJobs() throws Exception {
        log.info("Starting all Bank jobs execution");

        MDC.put(MDCKey.BANK.name(), Bank.AXIS.name());
        jobLauncher.run(missingAccountJob, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.AXIS_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob1, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob2, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob3, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakBankJob, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakBankNoPwdJob, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakImportedAccountJob, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.HDFC.name());
        jobLauncher.run(hdfcImportedAccountJob, new JobParametersBuilder()
                .addString(JobConstants.JOB_ID, String.valueOf(System.currentTimeMillis()))
                .addString(JobConstants.BATCH_OF, BatchOf.HDFC_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        log.debug("Completed all jobs execution");
        cacheService.evictCacheByName(CacheConfig.CacheName.TRANSACTION);
        cacheService.evictCacheByName(CacheConfig.CacheName.SUMMARY);

        generateCSVFile();
    }

    private void generateCSVFile() throws Exception {
        log.debug("Started writing csv report");

        // generate csv file now for Google Sheet
        List<Transaction> records = transactionRepository.findAll();
        Collections.sort(records, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        csvWriterForGoogleSheet.open(new ExecutionContext());
        log.info("Writing to file for Google Sheets, file {}", outputFileName);
        csvWriterForGoogleSheet.write(new Chunk<>(records));
        csvWriterForGoogleSheet.close();

        log.info("Completed writing csv report");
    }
}
