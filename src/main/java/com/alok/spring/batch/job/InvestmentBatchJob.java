package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.model.Investment;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableBatchProcessing
public class InvestmentBatchJob {
    @Value("file:${file.path.investment}")
    private Resource[] resources;

    @Value("${fields.name.investment:#{null}}")
    private String[] fieldNames;

    @Bean("InvestmentJob")
    public Job investmentJob(JobBuilderFactory jobBuilderFactory,
                          StepBuilderFactory stepBuilderFactory,
                          ItemReader<Investment> investmentItemsReader,
                          ItemProcessor<Investment, Investment> defaultInvestmentProcessor,
                          ItemWriter<Investment> investmentDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("Investment-ETL-file-load")
                .<Investment,Investment>chunk(100)
                .reader(investmentItemsReader)
                .processor(defaultInvestmentProcessor)
                .writer(investmentDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("Investment-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("Investment-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Investment> investmentItemsReader(@Qualifier("investmentItemReader") CSVReader investmentItemReader) {

        MultiResourceItemReader<Investment> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(investmentItemReader);
        return reader;
    }

    @Bean
    public CSVReader<Investment> investmentItemReader(@Qualifier("CSVReader") CSVReader<Investment> flatFileItemReader) {

        flatFileItemReader.setName("Investment-CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(investmentLineMapper());
        flatFileItemReader.setTransactionType("INVESTMENT");
        flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<Investment> investmentLineMapper() {
        DefaultLineMapper<Investment> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<Investment> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Investment.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}

