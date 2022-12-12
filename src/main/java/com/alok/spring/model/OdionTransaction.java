package com.alok.spring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class OdionTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate date;
    private String particular;
    private Account debitAccount;
    private Account creditAccount;
    private Double amount;

    public enum Account {
        ODION("Odion"),
        SAVING("Saving"),
        SBI_MAX_GAIN("SBI Max Gain"),
        INTEREST("Interest"),
        MISC("Misc"),
        ADARSH("Adarsh"),
        INTEREST_ADARSH("Interest_Adarsh"),
        OPENING_BALANCE("Opening Balance");

        private String name;

        Account(String name) {
            this.name = name;
        }

        public String getName() { return name; }

        @Override
        public String toString() { return name; }

        public static Account valueOfOrDefault(String myValue) {
            for(Account type : Account.class.getEnumConstants()) {
                if(type.toString().equals(myValue)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Account type not found: " + myValue);
        }

    }
}


