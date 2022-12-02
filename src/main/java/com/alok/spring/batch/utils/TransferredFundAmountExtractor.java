package com.alok.spring.batch.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class TransferredFundAmountExtractor extends DefaultFieldExtractor {

    @PostConstruct
    public void init() {
        super.setStringPatterns(new String[] {
                "(?<=TO)\\d+",
                "(?<=OUTWARD  ORG)\\d+",
                "(?<=IMPS-[0-9]{10,15} )\\d+",
                "(?<=UPI-[0-9]{10,15} )\\d+",
                "(?<=EBPP-[0-9]{10,15} )\\d+",
                "(?<=OUTWARD  REV)\\d+",
        });
    }
}
