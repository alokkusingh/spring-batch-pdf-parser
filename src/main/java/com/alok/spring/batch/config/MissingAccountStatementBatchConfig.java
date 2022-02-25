package com.alok.spring.batch.config;

import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
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
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class MissingAccountStatementBatchConfig {
    @Value("file:${file.path.missing_account}")
    private Resource[] resources;

    @Value("${fields.name.missing_account:#{null}}")
    private String[] fieldNames;

    @Bean("MissingAccountJob")
    public Job missingAccountJob(JobBuilderFactory jobBuilderFactory,
                          StepBuilderFactory stepBuilderFactory,
                          ItemReader<Transaction> itemsReader,
                          ItemProcessor<Transaction, Transaction> defaultAccountProcessor,
                          ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("MissingAccount-ETL-file-load")
                .<Transaction,Transaction>chunk(100)
                .reader(itemsReader)
                .processor(defaultAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("MissingAccount-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("MissingAccount-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }



    @Bean
    public MultiResourceItemReader<Transaction> itemsReader() {

        MultiResourceItemReader<Transaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(itemReader());
        return reader;
    }

    @Bean
    public FlatFileItemReader<Transaction> itemReader() {

        FlatFileItemReader<Transaction> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("MissingTransaction-CSV-Reader");
        //flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<Transaction> lineMapper() {
        DefaultLineMapper<Transaction> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<Transaction> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Transaction.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}

