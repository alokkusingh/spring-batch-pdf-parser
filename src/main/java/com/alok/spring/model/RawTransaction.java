package com.alok.spring.model;

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
    private String file;

    public RawTransaction() {
        lines = new LinkedList<>();
    }

    public String getMergedLines() {
        String str = String.join(", ", lines);
        int length = str.length()>252?252:str.length();

        return "=\"" + str.substring(0,length) + "\"" ;
    }
}
