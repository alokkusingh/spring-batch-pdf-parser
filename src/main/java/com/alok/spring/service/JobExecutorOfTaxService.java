package com.alok.spring.service;

import com.alok.spring.config.CacheConfig;
import com.alok.spring.constant.BatchOf;
import com.alok.spring.repository.ProcessedFileRepository;
import com.alok.spring.repository.TaxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobExecutorOfTaxService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("TaxJob")
    private Job taxJob;

    @Autowired
    private TaxRepository taxRepository;

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Autowired
    private CacheService cacheService;

    public void executeAllJobs() throws Exception {
        executeAllJobs(false);
    }

    public void executeAllJobs(boolean force) throws Exception {

        if (force) {
            log.info("Delete all the taxes first");
            taxRepository.deleteAll();
            processedFileRepository.deleteAllByType("TAX");
        }

        log.info("Starting job execution");

        jobLauncher.run(taxJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.TAX.name())
                .toJobParameters());

        log.debug("Completed job execution");
    }
}
