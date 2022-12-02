package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.batch.utils.BankUtils;
import com.alok.spring.model.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class HDFCImportedAccountStatementBatchJob {
    @Value("file:${file.path.hdfc_account.imported}")
    private Resource[] resources;

    @Value("${fields.name.hdfc_account.imported:#{null}}")
    private String[] fieldNames;

    private static final String JOB_NAME = "HDFCAccount-Imported-ETL-Job1";
    private static final String PROCESSOR_TASK_NAME = "HDFCAccount-Imported-ETL-Job1-file-load";
    private static final String ARCHIVE_TASK_NAME = "HDFCAccount-Imported-ETL-Job1-file-archive";

    @Bean("HDFCImportedAccountJob")
    public Job hdfcImportedAccountJob(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      ItemReader<Transaction> hdfcImportedItemsReader,
                                      ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                                      ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<Transaction,Transaction>chunk(100, transactionManager)
                .reader(hdfcImportedItemsReader)
                .processor(defaultAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();

        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = new StepBuilder(ARCHIVE_TASK_NAME, jobRepository)
                .tasklet(archiveTask, transactionManager)
                .build();

        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Transaction> hdfcImportedItemsReader(@Qualifier("hdfcImportedItemReader") CSVReader hdfcImportedItemReader) {

        MultiResourceItemReader<Transaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(hdfcImportedItemReader);
        return reader;
    }

    @Bean
    public CSVReader<Transaction> hdfcImportedItemReader(@Qualifier("CSVReader") CSVReader<Transaction> flatFileItemReader) {
        flatFileItemReader.setName("HDFCImportedAccount-CSV-Reader");
        flatFileItemReader.setLineMapper(BankUtils.importedAccountLineMapper(fieldNames, BankUtils.LineMapperType.HDFC));
        flatFileItemReader.setStrict(false);
        flatFileItemReader.setComments(new String[] {"#"});
        flatFileItemReader.setLinesToSkip(2);
        flatFileItemReader.setTransactionType("BANK");

        return flatFileItemReader;
    }
}
