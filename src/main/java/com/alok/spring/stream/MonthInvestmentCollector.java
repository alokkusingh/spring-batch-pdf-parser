package com.alok.spring.stream;

import com.alok.spring.model.Investment;
import com.alok.spring.response.GetInvestmentsResponse;

import java.time.YearMonth;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import lombok.Data;
import org.javatuples.Pair;
import org.javatuples.Quartet;

// Assumption the investment is sorted on YearMonth Asc
public class MonthInvestmentCollector implements
        Collector<
                Investment,
                Quartet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>,
                Quartet<Long, Long, Map<String, Long>, List<GetInvestmentsResponse.MonthInvestment>>
                > {
    @Override
    public Supplier<Quartet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>> supplier() {
        return () -> new Quartet<>(new MutableLong(), Pair.with(new MutableYearMonth(), new MutableLong()), new HashMap<>(), new HashMap<>());
    }

    @Override
    public BiConsumer<Quartet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>, Investment> accumulator() {
        return (investmentSummaryQuartet, investment) -> {
            var yearMonth = YearMonth.of(investment.getYearx(), investment.getMonthx());

            // Aggregate for total summary
            investmentSummaryQuartet.getValue0().add(investment.getContribution());
                // All Investment type as on value to be accumulated
            var lastMonthValue = investmentSummaryQuartet.getValue1();
            if (yearMonth.equals(lastMonthValue.getValue0().getValue())) {
                lastMonthValue.getValue1().add(investment.getValueAsOnMonth());
            } else {
                lastMonthValue.getValue0().setValue(yearMonth);
                lastMonthValue.getValue1().setValue(investment.getValueAsOnMonth());
            }

            // Aggregation for each investment type
            var categoryInvestmentMap = investmentSummaryQuartet.getValue2();
            categoryInvestmentMap.compute(investment.getHead(),
                    (head, value) -> value == null? investment.getContribution(): value + investment.getContribution()
            );

            // Aggregation fo the month
            // note - each month for each investment type there will one Investment object
            var yearMonthInvestmentMap = investmentSummaryQuartet.getValue3();
            if (!yearMonthInvestmentMap.containsKey(yearMonth)) {
                yearMonthInvestmentMap.put(yearMonth, GetInvestmentsResponse.MonthInvestment.builder()
                                .yearMonth(yearMonth.toString())
                                .investmentAmount(0L)
                                .asOnInvestment(0L)
                                .asOnValue(0L)
                                .investments(new ArrayList<>())
                        .build());
            }
            var monthInvestment = yearMonthInvestmentMap.get(yearMonth);
            monthInvestment.setInvestmentAmount(monthInvestment.getInvestmentAmount() + investment.getContribution());
            monthInvestment.setAsOnInvestment(investmentSummaryQuartet.getValue0().getValue());
            monthInvestment.setAsOnValue(monthInvestment.getAsOnValue() + investment.getValueAsOnMonth());
            monthInvestment.getInvestments().add(GetInvestmentsResponse.MonthInvestment.Investment.builder()
                    .head(investment.getHead())
                    .investmentAmount(investment.getContribution())
                    .asOnValue(investment.getValueAsOnMonth())
                    .asOnInvestment(Math.toIntExact(categoryInvestmentMap.get(investment.getHead())))
                    .build());
        };
    }

    @Override
    public BinaryOperator<Quartet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>> combiner() {
        return null;
    }

    @Override
    public Function<
            Quartet<MutableLong, Pair<MutableYearMonth, MutableLong>, Map<String, Long>, Map<YearMonth, GetInvestmentsResponse.MonthInvestment>>,
            Quartet<Long, Long, Map<String, Long>, List<GetInvestmentsResponse.MonthInvestment>>
            > finisher() {
        return (investmentSummaryQuartet) -> new Quartet<>(
                investmentSummaryQuartet.getValue0().getValue(),
                investmentSummaryQuartet.getValue1().getValue1().getValue(),
                investmentSummaryQuartet.getValue2(),
                new ArrayList(investmentSummaryQuartet.getValue3().values())
        );
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }


}

@Data
class MutableLong {
    private long value;

    public void add(long value) {
        this.value += value;
    }
}


@Data class MutableYearMonth {
    private YearMonth value;

    public MutableYearMonth() {
        value = YearMonth.of(2007, 1);
    }
}
