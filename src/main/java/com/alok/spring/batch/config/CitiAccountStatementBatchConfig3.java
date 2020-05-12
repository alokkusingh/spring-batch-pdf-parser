package com.alok.spring.batch.config;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.utils.DefaultLineExtractor;
import com.alok.spring.batch.utils.LineExtractor;
import com.alok.spring.batch.reader.PDFReader;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class CitiAccountStatementBatchConfig3 {
    @Value("file:${file.path.citi_account.password3}")
    private Resource[] resources;

    @Value("${file.password.citi.password3}")
    private String filePassword;

    @Bean("CitiBankJob3")
    public Job citiBankJob1(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<RawTransaction> citiItemsReader3,
                           ItemProcessor<RawTransaction, Transaction> citiBankAccountProcessor,
                           ItemWriter<Transaction> itemWriter
    ) {
        Step step1 = stepBuilderFactory.get("CitiAccount-ETL-file-load")
                .<RawTransaction,Transaction>chunk(1000)
                .reader(citiItemsReader3)
                .processor(citiBankAccountProcessor)
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
    public MultiResourceItemReader<RawTransaction> citiItemsReader3() {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader3());
        return reader;
    }

    @Bean
    public PDFReader citiItemReader3() {

        PDFReader flatFileItemReader = new PDFReader();
        flatFileItemReader.setName("CitiBank-CSV-Reader3");
        flatFileItemReader.setFilePassword(filePassword);

        LineExtractor defaultLineExtractor = new DefaultLineExtractor();
        defaultLineExtractor.setStartReadingText("Date Transaction.*");
        defaultLineExtractor.setEndReadingText("Banking Reward Points.*");
        defaultLineExtractor.setLinesToSkip(
            new String[] {
                   "^Your  Citibank  Account.*",
                   "^Statement  Period.*",
                    "^Page .*"
            }
        );

        flatFileItemReader.setLineExtractor(defaultLineExtractor);

        return flatFileItemReader;
    }
}
