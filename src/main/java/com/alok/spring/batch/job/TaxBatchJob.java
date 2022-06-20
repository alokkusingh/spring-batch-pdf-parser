package com.alok.spring.batch.job;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
import com.alok.spring.model.Tax;
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
public class TaxBatchJob {
    @Value("file:${file.path.tax}")
    private Resource[] resources;

    @Value("${fields.name.tax:#{null}}")
    private String[] fieldNames;

    @Bean("TaxJob")
    public Job taxJob(JobBuilderFactory jobBuilderFactory,
                          StepBuilderFactory stepBuilderFactory,
                          ItemReader<Tax> taxItemsReader,
                          ItemProcessor<Tax, Tax> defaultTaxProcessor,
                          ItemWriter<Tax> taxDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("Tax-ETL-file-load")
                .<Tax,Tax>chunk(1)
                .reader(taxItemsReader)
                .processor(defaultTaxProcessor)
                .writer(taxDbWriter)
                .build();

        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("Tax-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("Tax-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Tax> taxItemsReader(@Qualifier("taxItemReader") CSVReader taxItemReader) {

        MultiResourceItemReader<Tax> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(taxItemReader);
        return reader;
    }

    @Bean
    public CSVReader<Tax> taxItemReader(@Qualifier("CSVReader") CSVReader<Tax> flatFileItemReader) {

        flatFileItemReader.setName("Tax-CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(taxLineMapper());
        flatFileItemReader.setTransactionType("TAX");
        flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<Tax> taxLineMapper() {
        DefaultLineMapper<Tax> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<Tax> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Tax.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}

