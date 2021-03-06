package com.alok.spring.batch.utils;

import com.alok.spring.batch.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

@Slf4j
public class KotakImportedFieldSetMapper implements FieldSetMapper<Transaction> {

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) {
        log.debug("Mapping fieldSet to Transaction, SrlNo: {}", fieldSet.readString("slNo"));
        Transaction transaction = new Transaction();
        transaction.setDate(fieldSet.readDate("date", "dd/MM/yyyy"));
        transaction.setDescription(fieldSet.readString("description"));
        String drCr = fieldSet.readString("drCr");
        if ("DR".equals(drCr)) {
            transaction.setCredit((int) Math.round(fieldSet.readDouble("amount")));
            transaction.setDebit(0);
        } else {
            transaction.setDebit((int) Math.round(fieldSet.readDouble("amount")));
            transaction.setCredit(0);
        }

        log.debug("Mapped transaction {}", transaction);

        return transaction;
    }
}
