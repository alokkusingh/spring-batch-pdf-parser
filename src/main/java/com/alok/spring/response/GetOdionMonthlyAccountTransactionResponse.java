package com.alok.spring.response;

import com.alok.spring.model.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.YearMonth;
import java.util.Map;

@Data
@Builder
public class GetOdionMonthlyAccountTransactionResponse {

    private Map<OdionTransaction.Account, Map<YearMonth, Double>> accountMonthTransaction;
}
