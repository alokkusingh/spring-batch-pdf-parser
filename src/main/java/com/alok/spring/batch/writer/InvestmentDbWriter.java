package com.alok.spring.batch.writer;

import com.alok.spring.model.Investment;
import com.alok.spring.repository.InvestmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InvestmentDbWriter implements ItemWriter<List<Investment>> {

    @Autowired
    InvestmentRepository investmentRepository;

    @Override
    public void write(List<? extends List<Investment>> list) throws Exception {
        investmentRepository.saveAll(
                list.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }
}
