package com.alok.spring.batch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobExecutorService {

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

    public void executeAllJobs() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {

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
    }
}
