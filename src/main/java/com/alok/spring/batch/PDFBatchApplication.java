package com.alok.spring.batch;

import com.alok.spring.batch.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class PDFBatchApplication implements ApplicationRunner {

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	@Qualifier("CitiBankJob1")
	Job citiBankJob;

	public static void main(String[] args) {
		SpringApplication.run(PDFBatchApplication.class, args);
	}


	@Scheduled(cron = "0 * * 1 * ?")
	public void performCitiBankLoad() throws Exception
	{
		JobParameters params = new JobParametersBuilder()
				.addString("JobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(citiBankJob, params);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");
	}
}
