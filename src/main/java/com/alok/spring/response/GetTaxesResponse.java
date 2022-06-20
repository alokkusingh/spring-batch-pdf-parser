package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetTaxesResponse {

    private List<Tax> taxes;

    @Data
    @Builder
    public static class Tax {
        private Long id;
        private String financialYear;
        private Integer paidAmount;
        private Integer refundAmount;
    }
}
