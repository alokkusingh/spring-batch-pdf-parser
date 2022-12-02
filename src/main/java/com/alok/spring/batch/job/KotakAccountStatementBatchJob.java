package com.alok.spring.batch.job;

import com.alok.spring.model.RawTransaction;
import com.alok.spring.model.Transaction;
import com.alok.spring.batch.processor.BankAccountProcessor;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.repository.ProcessedFileRepository;
import com.alok.spring.batch.utils.KotakLineExtractor;
import com.alok.spring.batch.utils.LineExtractor;
import com.alok.spring.batch.reader.PDFReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;

@Configuration
@EnableBatchProcessing
public class KotakAccountStatementBatchJob {
    @Value("file:${file.path.kotak_account}")
    private Resource[] resources;

    @Value("${file.password.kotak}")
    private String filePassword;

    private ProcessedFileRepository processedFileRepository;

    private static final String JOB_NAME = "KotakAccount-ETL-Job1";
    private static final String PROCESSOR_TASK_NAME = "KotakAccount-ETL-Job1-file-load";
    private static final String ARCHIVE_TASK_NAME = "KotakAccount-ETL-Job1-file-archive";

    @Bean("KotakBankJob")
    public Job kotakBankJob(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            ItemReader<RawTransaction> kotakItemsReader,
                            ItemProcessor<RawTransaction, Transaction> kotakAccountProcessor,
                            ItemWriter<Transaction> bankAccountDbWriter,
                            ProcessedFileRepository processedFileRepository
    ) {
        this.processedFileRepository = processedFileRepository;

        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<RawTransaction,Transaction>chunk(100, transactionManager)
                .reader(kotakItemsReader)
                .processor(kotakAccountProcessor)
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
    public MultiResourceItemReader<RawTransaction> kotakItemsReader(PDFReader kotakItemReader) {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(kotakItemReader);
        return reader;
    }

    @Bean
    @DependsOn({"processedFileRepository"})
    public PDFReader kotakItemReader(@Qualifier("PDFReader") PDFReader flatFileItemReader) {

        //PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("KotakBank-PDF-Reader3");
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
