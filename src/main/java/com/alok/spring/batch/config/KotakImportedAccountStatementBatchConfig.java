package com.alok.spring.batch.config;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.utils.KotakImportedFieldSetMapper;
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
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class KotakImportedAccountStatementBatchConfig {
    @Value("file:${file.path.kotak_account.imported}")
    private Resource[] resources;

    @Value("${fields.name.kotak_account.imported:#{null}}")
    private String[] fieldNames;

    @Bean("KotakImportedAccountJob")
    public Job kotakImportedAccountJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<Transaction> kotakImportedItemsReader,
                           ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                           ItemWriter<Transaction> itemWriter
    ) {
        Step step1 = stepBuilderFactory.get("KotakImportedAccount-ETL-file-load")
                .<Transaction,Transaction>chunk(1000)
                .reader(kotakImportedItemsReader)
                .processor(defaultAccountProcessor)
                .writer(itemWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("KotakImportedAccount-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("KotakImportedAccount-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Transaction> kotakImportedItemsReader() {

        MultiResourceItemReader<Transaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(kotakImportedItemReader());
        return reader;
    }

    @Bean
    public FlatFileItemReader<Transaction> kotakImportedItemReader() {

        FlatFileItemReader<Transaction> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("KotakImportedAccount-CSV-Reader");
        flatFileItemReader.setLineMapper(kotakImportedAccountLineMapper());
        flatFileItemReader.setStrict(false);
        flatFileItemReader.setComments(new String[] {",", "\"", "#",
                "ALOK", "Bangalore", "KARNATAKA", "INDIA", "Opening", "Closing", "You"
        });
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<Transaction> kotakImportedAccountLineMapper() {
        DefaultLineMapper<Transaction> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        FieldSetMapper<Transaction> fieldSetMapper = new KotakImportedFieldSetMapper();

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}
