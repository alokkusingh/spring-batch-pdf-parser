package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.ProcessedFileRepository;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.utils.UploadType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
public class BankJobExecutorService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${dir.path.hdfc_account.imported}")
    private String hdfcExportDir;

    @Value("${dir.path.kotak_account.imported}")
    private String kotakExportDir;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("KotakImportedAccountJobV2")
    private Job kotakImportedAccountJobV2;

    @Autowired
    @Qualifier("HDFCImportedAccountJob")
    private Job hdfcImportedAccountJob;

    @Autowired
    private MultiResourceItemReader<Transaction> hdfcImportedItemsReader;

    @Autowired
    private MultiResourceItemReader<Transaction> kotakImportedItemsReaderV2;

    @Autowired
    private CacheService cacheService;

    @Autowired
    FlatFileItemWriter<Transaction> csvWriterForGoogleSheet;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    private String outputFileName;

    @Autowired
    public BankJobExecutorService(
            @Value("${file.export.google.sheet}")
                    String outputFileName
    ) {
        // outputFileName was required injection via constructor otherwise it was coming null
        // during csvWriterForGoogleSheet bean creation
        this.outputFileName = outputFileName;
    }

    public void executeBatchJob(UploadType uploadType, final String fileName) throws Exception {

        //log.info("Delete all the transactions first");
        log.info("Starting job execution");
        //transactionRepository.deleteAll();

        // No need to process all file - only new file process - so below line to be commented
        //processedFileRepository.deleteAllByType("BANK");

        // Only for below jobs file upload supported - others jobs will be executed at startup only

        // Job - Kotak Exported V2
        if (uploadType == UploadType.KotakExportedStatement) {
            kotakImportedItemsReaderV2.setResources(new Resource[]{
                    resourceLoader.getResource("file:" + kotakExportDir + "/" + fileName)
            });
            jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());
        }

        // Job - HDFC Exported
        if (uploadType == UploadType.HDFCExportedStatement) {
            hdfcImportedItemsReader.setResources(new Resource[]{
                    resourceLoader.getResource("file:" + hdfcExportDir + "/" + fileName)
            });
            jobLauncher.run(hdfcImportedAccountJob, new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());
        }

        log.debug("Completed job execution");
        cacheService.evictCacheByName(CacheConfig.CacheName.TRANSACTION);
        cacheService.evictCacheByName(CacheConfig.CacheName.SUMMARY);
        log.debug("Started writing csv report");

        // generate csv file now for Google Sheet
        List<Transaction> records = transactionRepository.findAll();
        Collections.sort(records, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        csvWriterForGoogleSheet.open(new ExecutionContext());
        log.info("Writing to file for Google Sheets, file {}", outputFileName);
        csvWriterForGoogleSheet.write(records);
        log.info("Write Completed!");
        csvWriterForGoogleSheet.close();

        log.info("Completed writing csv report");
    }
}
