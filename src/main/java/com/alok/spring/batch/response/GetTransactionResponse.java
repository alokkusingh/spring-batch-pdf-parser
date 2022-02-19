package com.alok.spring.batch.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class GetTransactionResponse {

    private Integer id;
    private Date date;
    private Integer debit;
    private Integer credit;
    private String head;
    private String description;

}
