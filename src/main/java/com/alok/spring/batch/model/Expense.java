package com.alok.spring.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Expense implements Comparable<Expense> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.DATE)
    private Date date;
    private String head;
    private Integer amount;
    private String comment;
    private String category;
    private Integer month;
    private Integer year;

    @Override
    public int compareTo(Expense o) {
        return this.date.compareTo(o.date);
    }

    public String getStrDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Transient
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", date=" + simpleDateFormat.format(date) +
                ", amount=" + amount +
                ", comment='" + comment + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
