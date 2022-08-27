package com.alok.spring.batch.utils;

import com.alok.spring.model.RawTransaction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class KotakLineExtractor extends DefaultLineExtractor {

    RawTransaction rawTransaction;
    @Override
    public boolean extractLine(String pageContent, List<RawTransaction> items, String file) {
        pageContent = pageContent.replaceAll("\\(Cr\\)", "\n");
        pageContent = pageContent.replaceAll("\\(Dr\\)", "\n");
        boolean start = false;
        if (startReadingText == null)
            start = true;

        for (String line: pageContent.split("\\r?\\n")) {
            line = line.trim();
            log.debug("Un-screened line: {}", line);
            if (shouldSkip(line))
                continue;

            if (!start) {
                if (line.matches(startReadingText)) {
                    log.info("Starting line processing after this line: {}", line);
                    start = true;
                }
            } else {
                if (endReadingText != null && line.matches(endReadingText)) {
                    log.info("Stopping line processing from this line: {}", line);
                    return false;
                }
                if (line.matches(dateRegex)) {
                    rawTransaction = new RawTransaction();
                    rawTransaction.setFile(file);
                    items.add(rawTransaction);
                }
                if (rawTransaction != null && !line.matches(startReadingText)) {
                    log.debug("Read line: {}", line);
                    rawTransaction.getLines().add(line);
                }
            }
        }

        return true;
    }
}
