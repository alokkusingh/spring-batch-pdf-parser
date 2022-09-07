package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class GetOdionAccountTransactionsResponse {

    private List<AccountTransaction> transactions;

    @Data
    @Builder
    public static class AccountTransaction {
        private Long id;
        private LocalDate date;
        private String particular;
        private double debit;
        private double credit;
    }
}
