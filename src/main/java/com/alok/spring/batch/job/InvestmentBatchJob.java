package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.model.Investment;
import com.alok.spring.model.RawInvestment;
import com.alok.spring.model.RawTransaction;
import com.alok.spring.model.Transaction;
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
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class InvestmentBatchJob {
    @Value("file:${file.path.investment}")
    private Resource[] resources;

    @Value("${fields.name.investment:#{null}}")
    private String[] fieldNames;

    private static final String JOB_NAME = "Investment-ETL-Load";
    private static final String PROCESSOR_TASK_NAME = "Investment-ETL-file-load";
    private static final String ARCHIVE_TASK_NAME = "Investment-ETL-file-archive";

    @Bean("InvestmentJob")
    public Job investmentJob(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             ItemReader<RawInvestment> investmentItemsReader,
                             ItemProcessor<RawInvestment, List<Investment>> defaultInvestmentProcessor,
                             ItemWriter<List<Investment>> investmentDbWriter
    ) {
        Step step1 = new StepBuilder(PROCESSOR_TASK_NAME, jobRepository)
                .<RawInvestment, List<Investment>>chunk(100, transactionManager)
                .reader(investmentItemsReader)
                .processor(defaultInvestmentProcessor)
                .writer(investmentDbWriter)
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
    public MultiResourceItemReader<RawInvestment> investmentItemsReader(@Qualifier("investmentItemReader") CSVReader investmentItemReader) {

        MultiResourceItemReader<RawInvestment> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(investmentItemReader);
        return reader;
    }

    @Bean
    public CSVReader<RawInvestment> investmentItemReader(@Qualifier("CSVReader") CSVReader<RawInvestment> flatFileItemReader) {

        flatFileItemReader.setName("Investment-CSV-Reader");
        flatFileItemReader.setLinesToSkip(2);
        flatFileItemReader.setLineMapper(investmentLineMapper());
        flatFileItemReader.setTransactionType("INVESTMENT");
        flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<RawInvestment> investmentLineMapper() {
        DefaultLineMapper<RawInvestment> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<RawInvestment> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(RawInvestment.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}

