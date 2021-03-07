package com.alok.spring.batch.utils;

import com.alok.spring.batch.reader.PDFReader;

public class CitiUtils {
    public static PDFReader getCitiItemReader(String filePassword) {

        PDFReader flatFileItemReader = new PDFReader();
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
