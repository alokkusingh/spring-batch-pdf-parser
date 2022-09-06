package com.alok.spring.response;

import com.alok.spring.model.OdionTransaction;
import lombok.Builder;
import lombok.Data;
import org.javatuples.Pair;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GetOdionAccountsBalanceResponse {

    private List<AccountBalance> accountBalances;

    @Data
    @Builder
    public static class AccountBalance {
        private OdionTransaction.Account account;
        private Double balance;
    }
}
