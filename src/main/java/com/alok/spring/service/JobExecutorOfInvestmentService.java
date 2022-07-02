package com.alok.spring.service;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.constant.BatchOf;
import com.alok.spring.repository.InvestmentRepository;
import com.alok.spring.repository.ProcessedFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobExecutorOfInvestmentService {

    private JobLauncher jobLauncher;
    private Job investmentJob;
    private InvestmentRepository investmentRepository;
    private ProcessedFileRepository processedFileRepository;

    public JobExecutorOfInvestmentService(
            JobLauncher jobLauncher, @Qualifier("InvestmentJob") Job investmentJob, InvestmentRepository investmentRepository,
            ProcessedFileRepository processedFileRepository
    ) {
        this.jobLauncher = jobLauncher;
        this.investmentJob = investmentJob;
        this.investmentRepository = investmentRepository;
        this.processedFileRepository = processedFileRepository;
    }

    public void executeAllJobs() throws Exception {
        executeAllJobs(false);
    }

    @LogExecutionTime
    public void executeAllJobs(boolean force) throws Exception {

        if (force) {
            log.info("Delete all the investment first");
            investmentRepository.deleteAll();
            processedFileRepository.deleteAllByType("INVESTMENT");
        }

        log.info("Starting job execution");

        jobLauncher.run(investmentJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("batchOf", BatchOf.INVESTMENT.name())
                .toJobParameters());

        log.debug("Completed job execution");
    }
}
