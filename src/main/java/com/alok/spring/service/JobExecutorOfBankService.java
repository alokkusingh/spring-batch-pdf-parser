package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.constant.Bank;
import com.alok.spring.constant.BatchOf;
import com.alok.spring.constant.MDCKey;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.ProcessedFileRepository;
import com.alok.spring.repository.TransactionRepository;
import com.alok.spring.constant.UploadType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class JobExecutorOfBankService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${dir.path.hdfc_account.imported}")
    private String hdfcExportDir;

    @Value("${dir.path.kotak_account.imported}")
    private String kotakExportDir;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("MissingAccountJob")
    private Job missingAccountJob;

    @Autowired
    @Qualifier("CitiBankJob1")
    private Job citiBankJob1;

    @Autowired
    @Qualifier("CitiBankJob2")
    private Job citiBankJob2;

    @Autowired
    @Qualifier("CitiBankJob3")
    private Job citiBankJob3;

    @Autowired
    @Qualifier("KotakBankJob")
    private Job kotakBankJob;

    @Autowired
    @Qualifier("KotakBankNoPwdJob")
    private Job kotakBankNoPwdJob;

    @Autowired
    @Qualifier("KotakImportedAccountJob")
    private Job kotakImportedAccountJob;

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
    public JobExecutorOfBankService(
            @Value("${file.export.google.sheet}")
                    String outputFileName
    ) {
        // outputFileName was required injection via constructor otherwise it was coming null
        // during csvWriterForGoogleSheet bean creation
        this.outputFileName = outputFileName;
    }

    public void executeBatchJob(UploadType uploadType, final String fileName) throws Exception {

        //log.info("Delete all the transactions first");
        log.info("Starting a Bank job execution");
        //transactionRepository.deleteAll();

        // No need to process all file - only new file process - so below line to be commented
        //processedFileRepository.deleteAllByType("BANK");

        // Only for below jobs file upload supported - others jobs will be executed at startup only

        // Job - Kotak Exported V2
        if (uploadType == UploadType.KotakExportedStatement) {
            MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
            kotakImportedItemsReaderV2.setResources(new Resource[]{
                    resourceLoader.getResource("file:" + kotakExportDir + "/" + fileName)
            });
            jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .addString("batchOf", BatchOf.KOTAK_BANK.name())
                    .toJobParameters());
            MDC.remove(MDCKey.BANK.name());
        }

        // Job - HDFC Exported
        if (uploadType == UploadType.HDFCExportedStatement) {
            MDC.put(MDCKey.BANK.name(), Bank.HDFC.name());
            hdfcImportedItemsReader.setResources(new Resource[]{
                    resourceLoader.getResource("file:" + hdfcExportDir + "/" + fileName)
            });
            jobLauncher.run(hdfcImportedAccountJob, new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .addString("batchOf", BatchOf.HDFC_BANK.name())
                    .toJobParameters());
            MDC.remove(MDCKey.BANK.name());
        }

        log.debug("Completed job execution");

        cacheService.evictCacheByName(CacheConfig.CacheName.TRANSACTION);
        cacheService.evictCacheByName(CacheConfig.CacheName.SUMMARY);

        generateCSVFile();
    }

    public void executeAllBatchJobs() throws Exception {
        log.info("Starting all Bank jobs execution");

        MDC.put(MDCKey.BANK.name(), Bank.AXIS.name());
        jobLauncher.run(missingAccountJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.AXIS_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob1, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob2, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.CITI.name());
        jobLauncher.run(citiBankJob3, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.CITI_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakBankJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakBankNoPwdJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakImportedAccountJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.KOTAK.name());
        jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.KOTAK_BANK.name())
                .toJobParameters());
        MDC.remove(MDCKey.BANK.name());

        MDC.put(MDCKey.BANK.name(), Bank.HDFC.name());
        jobLauncher.run(hdfcImportedAccountJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.HDFC_BANK.name())
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
        csvWriterForGoogleSheet.write(records);
        csvWriterForGoogleSheet.close();

        log.info("Completed writing csv report");
    }
}
