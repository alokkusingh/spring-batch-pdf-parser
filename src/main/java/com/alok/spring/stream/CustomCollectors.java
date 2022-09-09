package com.alok.spring.stream;

public class CustomCollectors {

    public static CategoryExpenseCollector toCategoryExpenseList() {
        return new CategoryExpenseCollector();
    }

    public static DayExpenseCollector toDayExpenseList() {
        return new DayExpenseCollector();
    }

    public static MonthInvestmentCollector toMonthInvestmentList() {
        return new MonthInvestmentCollector();
    }

    public static OdionAccountsBalanceCollector toOdionAccountsBalanceCollector() {
        return new OdionAccountsBalanceCollector();
    }

    public static OdionAccountsMonthlyTransactionCollector toOdionAccountsMonthlyTransactionCollector() {
        return new OdionAccountsMonthlyTransactionCollector();
    }

}
