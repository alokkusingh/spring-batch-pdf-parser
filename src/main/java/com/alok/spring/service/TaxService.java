package com.alok.spring.service;

import com.alok.spring.model.Tax;
import com.alok.spring.repository.TaxRepository;
import com.alok.spring.response.GetTaxesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaxService {

    private final TaxRepository taxRepository;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public TaxService(TaxRepository taxRepository) {
        this.taxRepository = taxRepository;
    }

    public GetTaxesResponse getAllTaxes() {
        log.info("All Taxes not available in cache");

        List<Tax> taxes = taxRepository.findAll();

        return GetTaxesResponse.builder()
                .taxes(taxes.stream()
                        .map(tax -> GetTaxesResponse.Tax.builder()
                                .id(tax.getId())
                                .financialYear(tax.getFinancialYear())
                                .paidAmount(tax.getPaidAmount())
                                .refundAmount(tax.getRefundAmount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void saveAllTaxes(List<Tax> taxRecords) {
        log.info("Delete all the taxes first");
        taxRepository.deleteAll();

        log.info("Save all the taxes");
        taxRepository.saveAll(taxRecords);
    }
}
