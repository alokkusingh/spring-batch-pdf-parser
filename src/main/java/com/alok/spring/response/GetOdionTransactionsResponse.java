package com.alok.spring.response;

import com.alok.spring.model.OdionTransaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetOdionTransactionsResponse {

    private List<OdionTransaction> transactions;
}
