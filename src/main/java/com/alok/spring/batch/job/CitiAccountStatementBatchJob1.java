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
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class CitiAccountStatementBatchJob1 {
    @Value("file:${file.path.citi_account.password1}")
    private Resource[] resources;

    @Value("${file.password.citi.password1}")
    private String filePassword;

    private ProcessedFileRepository processedFileRepository;

    private static final String JOB_NAME = "CitiAccount-ETL-Job1";
    private static final String PROCESSOR_TASK_NAME = "CitiAccount-ETL-Job1-file-load";
    private static final String ARCHIVE_TASK_NAME = "CitiAccount-ETL-Job1-file-archive";

    @Bean("CitiBankJob1")
    public Job citiBankJob1(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            ItemReader<RawTransaction> citiItemsReader1,
                            ItemProcessor<RawTransaction, Transaction> citiBankAccountProcessor,
                            ItemWriter<Transaction> bankAccountDbWriter,
                            ProcessedFileRepository processedFileRepository
    ) {
        this.processedFileRepository = processedFileRepository;

        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<RawTransaction,Transaction>chunk(100, transactionManager)
                .reader(citiItemsReader1)
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
    public MultiResourceItemReader<RawTransaction> citiItemsReader1(PDFReader citiItemReader1) {

        MultiResourceItemReader<RawTransaction> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader1);
        return reader;
    }

    @Bean
    @DependsOn({"processedFileRepository"})
    public PDFReader citiItemReader1(@Qualifier("PDFReader") PDFReader flatFileItemReader) {
        //return CitiUtils.getCitiItemReader(filePassword, processedFileRepository);
        //PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("CitiBank-PDF-Reader1");
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

    @Bean
    public LineMapper<String> citLineMapper() {
        DefaultLineMapper<String> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);

        BeanWrapperFieldSetMapper<String> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(String.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}
