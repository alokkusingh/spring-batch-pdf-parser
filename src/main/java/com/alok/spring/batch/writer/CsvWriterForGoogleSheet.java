package com.alok.spring.batch.writer;

import com.alok.spring.model.Transaction;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;

@Component
public class CsvWriterForGoogleSheet extends FlatFileItemWriter<Transaction> {

    private String outputFileName;

    public CsvWriterForGoogleSheet(@Value("${file.export.google.sheet}") String outputFileName) {
        // outputFileName was required injection via constructor otherwise it was coming null
        // during csvWriterForGoogleSheet bean creation
        this.outputFileName = outputFileName;

        WritableResource csvFile = new FileSystemResource(outputFileName);
        this.setResource(csvFile);
        this.setShouldDeleteIfExists(true);
        this.setHeaderCallback(writer -> writer.write("Srl. No.,Date,Head,Debit,Credit,Comment"));

        this.setLineAggregator(new DelimitedLineAggregator<Transaction>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<Transaction>() {
                    {
                        setNames(new String[] { "strDate", "strDate", "head", "debit", "credit", "description" });
                    }
                });
            }
        });
    }

}
