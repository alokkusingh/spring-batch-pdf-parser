package com.alok.spring.batch.writer;

import com.alok.spring.model.Tax;
import com.alok.spring.repository.TaxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TaxDbWriter implements ItemWriter<Tax> {

    @Autowired
    TaxRepository taxRepository;

    @Override
    public void write(List<? extends Tax> records) throws Exception {
        taxRepository.saveAll(records);
    }
}
