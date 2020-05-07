package com.alok.spring.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CitiBankAccountProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String line) {
        log.debug(line);
        return line;
    }
}
