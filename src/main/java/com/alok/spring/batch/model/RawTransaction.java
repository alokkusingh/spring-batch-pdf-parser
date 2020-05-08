package com.alok.spring.batch.model;

import lombok.*;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
@ToString
@Builder
public class RawTransaction {
    private List<String> lines;

    public RawTransaction() {
        lines = new LinkedList<>();
    }

    public String getMergedLines() {
        String str = String.join(", ", lines);
        int length = str.length()>255?255:str.length();

        return str.substring(0,length);
    }
}
