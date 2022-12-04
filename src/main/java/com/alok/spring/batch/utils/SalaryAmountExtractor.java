package com.alok.spring.batch.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SalaryAmountExtractor extends DefaultFieldExtractor {

    @PostConstruct
    public void init() {
        super.setStringPatterns(new String[] {
                // replace comma from pattern to empty
                "(?<=EVOLVING  SYSTEMS)\\d+",
                "(?<=EVOLVING  SYSTEMS )\\d+",
                "(?<=EVOLVING SYSTEMS )\\d+",
                "(?<=EVOLVING  SYSTEMS  NETWORKS  INDIA  PVT LTD )\\d+",
                "(?<=EVOLVING  SYSTEMS  NETWORKS  I  PVT  LTD)\\d+",
                "(?<=EVOLING  SYSTEMS  NETWORKS  INDIA  PVT LTD )\\d+",
                "(?<=NEFT  INWARD)\\d+",
                "(?<=TRF  FROM EVOLVING )\\d+",
                "(?<=NEFTINW-[0-9]{10,13} )\\d+",
        });
    }
}
