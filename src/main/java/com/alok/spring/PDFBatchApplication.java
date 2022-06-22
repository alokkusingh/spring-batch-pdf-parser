package com.alok.spring;

import com.alok.spring.model.Transaction;
import com.alok.spring.service.JobExecutorOfBankService;
import com.alok.spring.service.JobExecutorOfExpenseService;
import com.alok.spring.service.JobExecutorOfInvestmentService;
import com.alok.spring.service.JobExecutorOfTaxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class PDFBatchApplication implements ApplicationRunner {

	private JobExecutorOfBankService jobExecutorOfBankService;
	private JobExecutorOfExpenseService jobExecutorOfExpenseService;
	private JobExecutorOfTaxService jobExecutorOfTaxService;
	private JobExecutorOfInvestmentService jobExecutorOfInvestmentService;
	private FlatFileItemWriter<Transaction> csvWriterForGoogleSheet;

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

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");

		jobExecutorOfBankService.executeAllBatchJobs();
		jobExecutorOfExpenseService.executeAllJobs();
		jobExecutorOfTaxService.executeAllJobs();
		jobExecutorOfInvestmentService.executeAllJobs();
	}

	/*@Bean
	//@Order(2)
	public FlatFileItemWriter<Transaction> csvWriterForGoogleSheet(

			) {

		Resource csvFile = new FileSystemResource(outputFileName);
		FlatFileItemWriter csvWriter = new FlatFileItemWriter();
		csvWriter.setResource(csvFile);
		csvWriter.setShouldDeleteIfExists(true);
		csvWriter.setHeaderCallback(writer -> writer.write("Srl. No.,Date,Head,Debit,Credit,Comment"));

		csvWriter.setLineAggregator(new DelimitedLineAggregator<Transaction>() {
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Transaction>() {
					{
						setNames(new String[] { "strDate", "strDate", "head", "debit", "credit", "description" });
					}
				});
			}
		});

		return csvWriter;
	}*/
}
