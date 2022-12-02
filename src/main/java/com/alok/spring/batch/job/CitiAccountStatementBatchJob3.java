package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.PDFReader;
import com.alok.spring.batch.utils.DefaultLineExtractor;
import com.alok.spring.batch.utils.LineExtractor;
import com.alok.spring.model.RawTransaction;
import com.alok.spring.model.Transaction;
import com.alok.spring.repository.ProcessedFileRepository;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class CitiAccountStatementBatchJob3 {
    @Value("file:${file.path.citi_account.password3}")
    private Resource[] resources;

    @Value("${file.password.citi.password3}")
    private String filePassword;

    private ProcessedFileRepository processedFileRepository;

    private static final String JOB_NAME = "CitiAccount-ETL-Job3";
    private static final String PROCESSOR_TASK_NAME = "CitiAccount-ETL-Job3-file-load";
    private static final String ARCHIVE_TASK_NAME = "CitiAccount-ETL-Job3-file-archive";

    @Bean("CitiBankJob3")
    public Job citiBankJob1(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            ItemReader<RawTransaction> citiItemsReader3,
                            ItemProcessor<RawTransaction, Transaction> citiBankAccountProcessor,
                            ItemWriter<Transaction> bankAccountDbWriter,
                            ProcessedFileRepository processedFileRepository
    ) {
        this.processedFileRepository = processedFileRepository;

        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<RawTransaction,Transaction>chunk(100, transactionManager)
                .reader(citiItemsReader3)
                .processor(citiBankAccountProcessor)
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
    public MultiResourceItemReader<RawTransaction> citiItemsReader3(PDFReader citiItemReader3) {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader3);
        return reader;
    }

    @Bean
    @DependsOn({"processedFileRepository"})
    public PDFReader citiItemReader3(@Qualifier("PDFReader") PDFReader flatFileItemReader) {

        //return CitiUtils.getCitiItemReader(filePassword, processedFileRepository);
        //PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("CitiBank-PDF-Reader3");
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
