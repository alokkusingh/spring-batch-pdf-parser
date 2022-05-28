package com.alok.spring.batch.utils;

import com.alok.spring.model.RawTransaction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultLineExtractor implements LineExtractor {
    protected String startReadingText;
    protected String[] linesToSkip;
    protected String dateRegex = "^[0-9]{2}[a-zA-Z]{3}[0-9]{2}.*";
    protected String endReadingText;
    private RawTransaction rawTransaction;

    @Override
    public void setDateRegex(String dateRegex) { this.dateRegex = dateRegex; }

    @Override
    public void setLinesToSkip(String[] linesToSkip) {
        this.linesToSkip = linesToSkip;
    }

    @Override
    public void setStartReadingText(String startReadingText) {
        this.startReadingText = startReadingText;
    }

    @Override
    public void setEndReadingText(String endReadingText) {
        this.endReadingText = endReadingText;
    }

    @Override
    public boolean extractLine(String pageContent, List<RawTransaction> items, String file) {
        boolean start = false;
        boolean stop = false;
        if (startReadingText == null)
            start = true;

        for (String line: pageContent.split("\\r?\\n")) {
            line = line.trim();
            log.debug("Un-screened line: {}", line);
            if (shouldSkip(line))
                continue;

            if (!start) {
                if (line.matches(startReadingText)) {
                    log.debug("Starting line processing after this line: {}", line);
                    start = true;
                }
            } else {
                if (endReadingText != null && line.matches(endReadingText)) {
                    log.debug("Stopping line processing from this line: {}", line);
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

    protected boolean shouldSkip(String line) {
        if (line.trim().isEmpty())
            return true;

        if (linesToSkip != null) {
            for (String skipLine: linesToSkip) {
                if (line.matches(skipLine)) {
                    return true;
                }
            }
        }

        return false;
    }
}
