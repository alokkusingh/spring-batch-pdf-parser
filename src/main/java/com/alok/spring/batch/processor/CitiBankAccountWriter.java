package com.alok.spring.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CitiBankAccountWriter implements ItemWriter<String> {

    @Override
    public void write(List<? extends String> lines) throws Exception {
    }
}
