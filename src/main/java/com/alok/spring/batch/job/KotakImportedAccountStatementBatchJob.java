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
public class KotakImportedAccountStatementBatchJob {
    @Value("file:${file.path.kotak_account.imported}")
    private Resource[] resources;

    @Value("${fields.name.kotak_account.imported:#{null}}")
    private String[] fieldNames;

    private static final String JOB_NAME = "KotakAccount-Imported-ETL-Job3";
    private static final String PROCESSOR_TASK_NAME = "KotakAccount-Imported-ETL-Job3-file-load";
    private static final String ARCHIVE_TASK_NAME = "KotakAccount-Imported-ETL-Job3-file-archive";

    @Bean("KotakImportedAccountJob")
    public Job kotakImportedAccountJob(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       ItemReader<Transaction> kotakImportedItemsReader,
                                       ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                                       ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<Transaction,Transaction>chunk(100, transactionManager)
                .reader(kotakImportedItemsReader)
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
    public MultiResourceItemReader<Transaction> kotakImportedItemsReader(@Qualifier("kotakImportedItemReader") CSVReader kotakImportedItemReader) {

        MultiResourceItemReader<Transaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(kotakImportedItemReader);
        return reader;
    }

    @Bean
    public CSVReader<Transaction> kotakImportedItemReader(@Qualifier("CSVReader") CSVReader<Transaction> flatFileItemReader) {
        //return KotakUtils.kotakImportedItemReader(fieldNames);
        //FlatFileItemReader<Transaction> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("KotakImportedAccount-CSV-Reader");
        flatFileItemReader.setLineMapper(BankUtils.importedAccountLineMapper(fieldNames, BankUtils.LineMapperType.KOTAK));
        flatFileItemReader.setStrict(false);
        flatFileItemReader.setComments(new String[] {",", "\"", "#",
                "ALOK", "Bangalore", "KARNATAKA", "INDIA", "Opening", "Closing", "You",
                "202", "Doddakannalli", "SArjapur", "Bengaluru", "Karnataka", "India", "560035"
        });
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setTransactionType("BANK");

        return flatFileItemReader;
    }
}
