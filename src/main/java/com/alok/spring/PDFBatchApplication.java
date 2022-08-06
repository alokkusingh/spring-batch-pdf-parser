package com.alok.spring;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.service.JobExecutorOfBankService;
import com.alok.spring.service.JobExecutorOfExpenseService;
import com.alok.spring.service.JobExecutorOfInvestmentService;
import com.alok.spring.service.JobExecutorOfTaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan({"com.alok.spring.mqtt.config", "com.alok.spring.config"})
@EnableScheduling
@SpringBootApplication
@Slf4j
public class PDFBatchApplication implements ApplicationRunner {

	private JobExecutorOfBankService jobExecutorOfBankService;
	private JobExecutorOfExpenseService jobExecutorOfExpenseService;
	private JobExecutorOfTaxService jobExecutorOfTaxService;
	private JobExecutorOfInvestmentService jobExecutorOfInvestmentService;

	@Autowired
	public PDFBatchApplication(
			JobExecutorOfBankService jobExecutorOfBankService, JobExecutorOfExpenseService jobExecutorOfExpenseService,
			JobExecutorOfTaxService jobExecutorOfTaxService, JobExecutorOfInvestmentService jobExecutorOfInvestmentService
	) {
		this.jobExecutorOfBankService = jobExecutorOfBankService;
		this.jobExecutorOfExpenseService = jobExecutorOfExpenseService;
		this.jobExecutorOfTaxService = jobExecutorOfTaxService;
		this.jobExecutorOfInvestmentService = jobExecutorOfInvestmentService;

	}

	public static void main(String[] args) {
		SpringApplication.run(PDFBatchApplication.class, args);
	}

	@LogExecutionTime
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");
		System.out.println("Application Started!!!");

		jobExecutorOfBankService.executeAllBatchJobs();
		jobExecutorOfExpenseService.executeAllJobs();
		jobExecutorOfTaxService.executeAllJobs();
		jobExecutorOfInvestmentService.executeAllJobs();

		log.info("All jobs completed!!!");
		System.out.println("All jobs completed!!!");
	}
}
