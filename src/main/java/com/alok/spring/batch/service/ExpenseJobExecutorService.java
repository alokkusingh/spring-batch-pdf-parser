package com.alok.spring.batch.service;

import com.alok.spring.batch.repository.ExpenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExpenseJobExecutorService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ExpenseJob")
    private Job expenseJob;

    @Autowired
    private ExpenseRepository expenseRepository;

    public void executeAllJobs() throws Exception {

        log.info("Delete all the transactions first");
        log.info("Starting job execution");
        expenseRepository.deleteAll();

        jobLauncher.run(expenseJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());

        log.debug("Completed job execution");
    }
}
