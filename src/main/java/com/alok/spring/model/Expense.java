package com.alok.spring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Temporal;
import jakarta.persistence.Transient;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    private String head;
    private Double amount;
    private String comment;
    private String category;
    private Integer monthx;
    private Integer yearx;

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
