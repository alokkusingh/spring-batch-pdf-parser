package com.alok.spring.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GetSalaryByCompanyResponse {
    private Integer total;
    private List<CompanySalary> companySalaries;

    @Data
    @Builder
    public static class CompanySalary {
        private String company;
        private Integer total;
        private List<MonthSalary> monthSalaries;

        @Data
        @Builder
        public static class MonthSalary implements Comparable<MonthSalary> {
            private Integer year;
            private Integer month;
            private Integer amount;

            @Override
            public int compareTo(MonthSalary o) {
                if (this.year < o.year)
                    return 1;

                if (this.year > o.year)
                    return -1;

                if (this.month < o.month)
                    return 1;

                if (this.month > o.month)
                    return -1;

                return 0;
            }
        }
    }
}
