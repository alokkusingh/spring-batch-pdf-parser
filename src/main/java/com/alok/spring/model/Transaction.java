package com.alok.spring.model;

import lombok.*;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Transaction implements Comparable<Transaction> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Temporal(TemporalType.DATE)
    private Date date;
    private Integer debit;
    private Integer credit;
    private String head;
    private String description;
    private boolean isSalary;
    private String file;

    @Transient
    private String strDate;

    @Transient
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public int compareTo(Transaction o) {
        return this.date.compareTo(o.date);
    }

    public String getStrDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + simpleDateFormat.format(date) +
                ", debit='" + debit + '\'' +
                ", credit='" + credit + '\'' +
                ", head='" + head + '\'' +
                ", description='" + description + '\'' +
                ", isSalary=" + isSalary +
                '}';
    }
}
