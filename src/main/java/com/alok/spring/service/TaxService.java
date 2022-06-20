package com.alok.spring.service;

import com.alok.spring.model.Tax;
import com.alok.spring.repository.TaxRepository;
import com.alok.spring.response.GetTaxesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaxService {

    @Autowired
    private TaxRepository taxRepository;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

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
}
