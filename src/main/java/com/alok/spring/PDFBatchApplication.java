package com.alok.spring;

import com.alok.spring.model.Transaction;
import com.alok.spring.service.JobExecutorOfBankService;
import com.alok.spring.service.JobExecutorOfExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class PDFBatchApplication implements ApplicationRunner {

	@Autowired
	private JobExecutorOfBankService jobExecutorOfBankService;

	@Autowired
	private JobExecutorOfExpenseService jobExecutorOfExpenseService;

	@Autowired
	FlatFileItemWriter<Transaction> csvWriterForGoogleSheet;

	//@Value("${file.export.google.sheet}")
	private String outputFileName;

	@Autowired
	public PDFBatchApplication(
			@Value("${file.export.google.sheet}")
					String outputFileName
	) {
		// outputFileName was required injection via constructor otherwise it was coming null
		// during csvWriterForGoogleSheet bean creation
		this.outputFileName = outputFileName;
	}


	public static void main(String[] args) {
		SpringApplication.run(PDFBatchApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application Started!!!");

		jobExecutorOfBankService.executeAllBatchJobs();
		jobExecutorOfExpenseService.executeAllJobs();
	}

	@Bean
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
	}
}
