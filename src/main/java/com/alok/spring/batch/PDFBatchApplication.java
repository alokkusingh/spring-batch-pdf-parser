package com.alok.spring.batch;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collections;
import java.util.List;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class PDFBatchApplication implements ApplicationRunner {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	FlatFileItemWriter<Transaction> csvWriterForGoogleSheet;

	@Autowired
	@Qualifier("CitiBankJob1")
	private Job citiBankJob;

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

		List<Transaction> records = transactionRepository.findAll();
		Collections.sort(records, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
		csvWriterForGoogleSheet.open(new ExecutionContext());
		log.info("Writing to file for Google Sheets, file {}", outputFileName);
		csvWriterForGoogleSheet.write(records);
		log.info("Write Completed!");
		csvWriterForGoogleSheet.close();
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
						setNames(new String[] { "id", "date", "head", "debit", "credit", "description" });
					}
				});
			}
		});

		return csvWriter;
	}
}
