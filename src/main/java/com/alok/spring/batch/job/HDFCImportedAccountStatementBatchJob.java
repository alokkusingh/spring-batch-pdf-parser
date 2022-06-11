package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.batch.utils.BankUtils;
import com.alok.spring.model.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class HDFCImportedAccountStatementBatchJob {
    @Value("file:${file.path.hdfc_account.imported}")
    private Resource[] resources;

    @Value("${fields.name.hdfc_account.imported:#{null}}")
    private String[] fieldNames;

    @Bean("HDFCImportedAccountJob")
    public Job hdfcImportedAccountJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<Transaction> hdfcImportedItemsReader,
                           ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                           ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("HDFCAccount-Imported-ETL-Job1-file-load")
                .<Transaction,Transaction>chunk(1000)
                .reader(hdfcImportedItemsReader)
                .processor(defaultAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("HDFCAccount-Imported-ETL-Job1-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("HDFCAccount-Imported-ETL-Job1")
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
