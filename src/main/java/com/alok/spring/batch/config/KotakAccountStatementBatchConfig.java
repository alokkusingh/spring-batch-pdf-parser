package com.alok.spring.batch.config;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.processor.BankAccountProcessor;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.repository.ProcessedFileRepository;
import com.alok.spring.batch.utils.KotakLineExtractor;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.text.SimpleDateFormat;

@Configuration
@EnableBatchProcessing
public class KotakAccountStatementBatchConfig {
    @Value("file:${file.path.kotak_account}")
    private Resource[] resources;

    @Value("${file.password.kotak}")
    private String filePassword;

    @Bean("KotakBankJob")
    public Job kotakBankJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<RawTransaction> kotakItemsReader,
                           ItemProcessor<RawTransaction, Transaction> kotakAccountProcessor,
                           ItemWriter<Transaction> bankAccountDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("CitiAccount-ETL-file-load")
                .<RawTransaction,Transaction>chunk(1000)
                .reader(kotakItemsReader)
                .processor(kotakAccountProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("KotakAccount-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("Kotak-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }



    @Bean
    public MultiResourceItemReader<RawTransaction> kotakItemsReader(PDFReader kotakItemReader) {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(kotakItemReader);
        return reader;
    }

    @Bean
    public PDFReader kotakItemReader(@Qualifier("PDFReader") PDFReader flatFileItemReader) {

        //PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("KotakBank-CSV-Reader3");
        flatFileItemReader.setFilePassword(filePassword);

        LineExtractor kotakLineExtractor = new KotakLineExtractor();
        kotakLineExtractor.setStartReadingText("Date.*Narration.*");
        kotakLineExtractor.setEndReadingText(".*Statement.*Summary.*");
        kotakLineExtractor.setLinesToSkip(
            new String[] {
                   ".*OPENING BALANCE.*",
                   "^Statement  Period.*",
                    "^Page .*"
            }
        );
        kotakLineExtractor.setDateRegex("^[0-9]{2}-[a-zA-Z]{3}-[0-9]{2}.*");

        flatFileItemReader.setLineExtractor(kotakLineExtractor);

        return flatFileItemReader;
    }

    @Bean
    public BankAccountProcessor kotakAccountProcessor() {
        BankAccountProcessor kotakAccountPrsr = new BankAccountProcessor();
        kotakAccountPrsr.setDateFiledLength(9);
        kotakAccountPrsr.setSimpleDateFormat(new SimpleDateFormat("dd-MMM-yy"));

        return kotakAccountPrsr;
    }
}
