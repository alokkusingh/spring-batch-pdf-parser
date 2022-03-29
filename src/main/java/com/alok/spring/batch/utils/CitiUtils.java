package com.alok.spring.batch.utils;

import com.alok.spring.batch.reader.PDFReader;
import com.alok.spring.batch.repository.ProcessedFileRepository;

public class CitiUtils {
    public static PDFReader getCitiItemReader(String filePassword, ProcessedFileRepository processedFileRepository) {

        PDFReader flatFileItemReader = new PDFReader(processedFileRepository);
        flatFileItemReader.setName("CitiBank-CSV-Reader2");
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
