package com.alok.spring.batch.config;

import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.processor.PDFReader;
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
public class CitiAccountStatementBatchConfig {
    @Value("file:${file.path.citi_account}")
    private Resource[] resources;

    @Value("${fields.name.student:#{null}}")
    private String[] fieldNames;

    @Value("${file.password.citi}")
    private String filePassword;

    @Bean("CitiBankJob")
    public Job citiBankJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory,
                           ItemReader<String> citiItemsReader,
                           ItemProcessor<String, String> citItemProcessor,
                           ItemWriter<String> itemWriter
    ) {
        Step step1 = stepBuilderFactory.get("CitiAccount-ETL-file-load")
                .<String,String>chunk(100)
                .reader(citiItemsReader)
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
    public MultiResourceItemReader<String> citiItemsReader() {

        MultiResourceItemReader<String> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(citiItemReader());
        return reader;
    }

    @Bean
    public PDFReader<String> citiItemReader() {

        PDFReader<String> flatFileItemReader = new PDFReader<String>();
        flatFileItemReader.setName("CitiBank-CSV-Reader");
        flatFileItemReader.setFilePassword(filePassword);
        //flatFileItemReader.setLinesToSkip(1);
        //flatFileItemReader.setLineMapper(lineMapper());
        //flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<String> citLineMapper() {
        DefaultLineMapper<String> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        //lineTokenizer.setNames(new String[]{"id", "name", "department", "marks"});
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<String> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(String.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}
