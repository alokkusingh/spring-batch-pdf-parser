package com.alok.spring.batch.job;

import com.alok.spring.model.Expense;
import com.alok.spring.batch.processor.FileArchiveTasklet;
import com.alok.spring.batch.reader.CSVReader;
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
public class ExpenseBatchJob {
    @Value("file:${file.path.expense}")
    private Resource[] resources;

    @Value("${fields.name.expense:#{null}}")
    private String[] fieldNames;

    @Bean("ExpenseJob")
    public Job expenseJob(JobBuilderFactory jobBuilderFactory,
                          StepBuilderFactory stepBuilderFactory,
                          ItemReader<Expense> expenseItemsReader,
                          ItemProcessor<Expense, Expense> defaultExpenseProcessor,
                          ItemWriter<Expense> bankAccountDbWriter
    ) {
        Step step1 = stepBuilderFactory.get("Expense-ETL-file-load")
                .<Expense,Expense>chunk(100)
                .reader(expenseItemsReader)
                .processor(defaultExpenseProcessor)
                .writer(bankAccountDbWriter)
                .build();


        FileArchiveTasklet archiveTask = new FileArchiveTasklet();
        archiveTask.setResources(resources);
        Step step2 = stepBuilderFactory.get("Expense-ETL-file-archive")
                .tasklet(archiveTask)
                .build();

        return jobBuilderFactory.get("Expense-ETL-Load")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }



    @Bean
    public MultiResourceItemReader<Expense> expenseItemsReader(@Qualifier("expenseItemReader") CSVReader expenseItemReader) {

        MultiResourceItemReader<Expense> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setStrict(false);
        reader.setDelegate(expenseItemReader);
        return reader;
    }

    @Bean
    public CSVReader<Expense> expenseItemReader(@Qualifier("CSVReader") CSVReader<Expense> flatFileItemReader) {

        //FlatFileItemReader<Expense> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("Expense-CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(expenseLineMapper());
        flatFileItemReader.setTransactionType("EXPENSE");
        flatFileItemReader.setStrict(false);

        return flatFileItemReader;
    }

    @Bean
    public LineMapper<Expense> expenseLineMapper() {
        DefaultLineMapper<Expense> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<Expense> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Expense.class);

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}

