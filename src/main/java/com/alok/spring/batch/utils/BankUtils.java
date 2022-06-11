package com.alok.spring.batch.utils;

import com.alok.spring.model.Transaction;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class BankUtils {

    public enum LineMapperType {
        KOTAK,
        HDFC
    }

    public static LineMapper<Transaction> importedAccountLineMapper(String[] fieldNames, LineMapperType lineMapperType) {
        DefaultLineMapper<Transaction> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(fieldNames);

        FieldSetMapper<Transaction> fieldSetMapper = null;

        if (lineMapperType == LineMapperType.KOTAK)
            fieldSetMapper = new KotakImportedFieldSetMapper();
        else if (lineMapperType == LineMapperType.HDFC)
            fieldSetMapper = new HDFCImportedFieldSetMapper();

        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }
}
