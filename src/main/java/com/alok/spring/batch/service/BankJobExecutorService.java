package com.alok.spring.batch.service;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.repository.ProcessedFileRepository;
import com.alok.spring.batch.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class BankJobExecutorService {

    @Autowired
    private JobLauncher jobLauncher;

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
    @Qualifier("MissingAccountJob")
    private Job missingAccountJob;

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

    public void executeAllJobs() throws Exception {

        log.info("Delete all the transactions first");
        log.info("Starting job execution");
        transactionRepository.deleteAll();

        // No need to process all file - only new file process - so below line to be committed
        processedFileRepository.deleteAllByType("BANK");

        jobLauncher.run(citiBankJob1, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(citiBankJob2, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(citiBankJob3, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(kotakBankJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(kotakBankNoPwdJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(kotakImportedAccountJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(kotakImportedAccountJobV2, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        jobLauncher.run(missingAccountJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        log.debug("Completed job execution");
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
