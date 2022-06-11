package com.alok.spring.batch.utils;

import com.alok.spring.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

@Slf4j
public class HDFCImportedFieldSetMapper implements FieldSetMapper<Transaction> {

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) {
        log.debug("Mapping fieldSet to Transaction, narration: {}", fieldSet.readString("narration"));
        Transaction transaction = new Transaction();
        String strDate = fieldSet.readString("date");
        transaction.setDate(fieldSet.readDate("date", Utility.getDateFormat(strDate)));
        transaction.setDescription(fieldSet.readString("narration"));
        transaction.setCredit((int) Math.round(fieldSet.readDouble("debitAmount")));
        transaction.setDebit((int) Math.round(fieldSet.readDouble("creditAmount")));

        log.debug("Mapped transaction {}", transaction);

        return transaction;
    }
}
