package com.alok.spring.batch.config;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.utils.PDFReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
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
public class CitiAccountStatementBatchConfig2 {
    @Value("file:${file.path.citi_account.password2}")
    private Resource[] resources;

    @Value("${file.password.citi.password2}")
    private String filePassword;

    @Bean("CitiBankJob2")
    public Job citiBankJob1(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<RawTransaction> citiItemsReader2,
                           ItemProcessor<RawTransaction, Transaction> citItemProcessor,
                           ItemWriter<Transaction> itemWriter
    ) {
        Step step1 = stepBuilderFactory.get("CitiAccount-ETL-file-load")
                .<RawTransaction,Transaction>chunk(1000)
                .reader(citiItemsReader2)
                .processor(citItemProcessor)
                .writer(itemWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("CitiAccount-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("Student-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }



    @Bean
    public MultiResourceItemReader<RawTransaction> citiItemsReader2() {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader2());
        return reader;
    }

    @Bean
    public PDFReader citiItemReader2() {

        PDFReader flatFileItemReader = new PDFReader();
        flatFileItemReader.setName("CitiBank-CSV-Reader2");
        flatFileItemReader.setFilePassword(filePassword);
        flatFileItemReader.setStartReadingText("Date Transaction.*");
        flatFileItemReader.setEndReadingText("CLOSING  BALANCE.*");
        flatFileItemReader.setLinesToSkip(
            new String[] {
                   "^Your  Citibank  Account.*",
                   "^Statement  Period.*",
                    "^Page .*"
            }
        );

        return flatFileItemReader;
    }
}
