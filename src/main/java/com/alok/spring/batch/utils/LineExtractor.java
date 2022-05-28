package com.alok.spring.batch.utils;

import com.alok.spring.model.RawTransaction;

import java.util.List;

public interface LineExtractor {

    public void setDateRegex(String dateRegex);

    public void setLinesToSkip(String[] linesToSkip);

    public void setStartReadingText(String startReadingText);

    public void setEndReadingText(String endReadingText);

    public boolean extractLine(String pageContent, List<RawTransaction> items, String file);
}
