package com.alok.spring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearMonth implements Comparable<YearMonth> {

    private Integer year;
    private Integer month;

    @Override
    public int compareTo(@NotNull YearMonth o) {
        if (o.year < this.year)
            return -1;
        if (o.year > this.year)
            return 1;

        if (o.month < this.month)
            return -1;
        if (o.month > this.month)
            return 1;

        return 0;
    }
}
