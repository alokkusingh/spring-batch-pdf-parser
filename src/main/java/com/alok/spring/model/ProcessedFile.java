package com.alok.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ProcessedFile implements Comparable<ProcessedFile> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    @Temporal(TemporalType.DATE)
    private Date date;
    private Integer records;
    private String type;

    @Transient
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public int compareTo(ProcessedFile o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + simpleDateFormat.format(date) + '\'' +
                ", records=" + records + '\'' +
                '}';
    }
}
