package com.alok.spring.batch.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DefaultFieldExtractor {

    private List<Pattern> patterns;
    private String[] stringPatterns;

    public DefaultFieldExtractor() {
        stringPatterns = new String[] {
        };

        patterns = new LinkedList<>();
        for (String strPattern: stringPatterns) {
            patterns.add(Pattern.compile(strPattern));
        }
    }

    public void setStringPatterns(String[] stringPatterns) {
        this.stringPatterns = stringPatterns;
        patterns = new LinkedList<>();
        for (String strPattern: stringPatterns) {
            patterns.add(Pattern.compile(strPattern));
        }
    }

    public String getField(String line) {
        Matcher m = null;
       for (Pattern pattern: patterns) {
           m = pattern.matcher(line);
           if (m.find()) {
               return m.group();
           }
       }

        return "";
    }
}
