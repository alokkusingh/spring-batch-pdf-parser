package com.alok.spring.batch.config;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.batch.utils.KotakUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class KotakImportedAccountStatementBatchConfigV2 {
    @Value("file:${file.path.kotak_account.imported.v2}")
    private Resource[] resources;

    @Value("${fields.name.kotak_account.imported.v2:#{null}}")
    private String[] fieldNames;

    @Bean("KotakImportedAccountJobV2")
    public Job kotakImportedAccountJobV2(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<Transaction> kotakImportedItemsReaderV2,
                           ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                           ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("KotakAccount-Imported-ETL-Job4-file-load")
                .<Transaction,Transaction>chunk(1000)
                .reader(kotakImportedItemsReaderV2)
                .processor(defaultAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("KotakAccount-Imported-ETL-Job4-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("KotakAccount-Imported-ETL-Job4")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Transaction> kotakImportedItemsReaderV2(@Qualifier("kotakImportedItemReaderV2") CSVReader kotakImportedItemReaderV2) {

        MultiResourceItemReader<Transaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(kotakImportedItemReaderV2);
        return reader;
    }

    @Bean
    public CSVReader<Transaction> kotakImportedItemReaderV2(@Qualifier("CSVReader") CSVReader<Transaction> flatFileItemReader) {

        // return KotakUtils.kotakImportedItemReader(fieldNames);
        flatFileItemReader.setName("KotakImportedAccount-CSV-Reader");
        flatFileItemReader.setLineMapper(KotakUtils.kotakImportedAccountLineMapper(fieldNames));
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