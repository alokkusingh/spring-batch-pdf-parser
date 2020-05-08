package com.alok.spring.batch.model;

import lombok.*;

import javax.persistence.*;
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
    Integer id;
    Date date;
    String debit;
    String credit;
    String head;
    String description;
    boolean isSalary;

    @Transient
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public int compareTo(Transaction o) {
        return this.date.compareTo(o.date);
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
