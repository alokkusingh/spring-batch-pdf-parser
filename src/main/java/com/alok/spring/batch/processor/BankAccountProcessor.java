package com.alok.spring.batch.processor;

import com.alok.spring.batch.utils.DefaultFieldExtractor;
import com.alok.spring.batch.utils.Utility;
import com.alok.spring.constant.MDCKey;
import com.alok.spring.model.RawTransaction;
import com.alok.spring.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("citiBankAccountProcessor")
@Slf4j
public class BankAccountProcessor implements ItemProcessor<RawTransaction, Transaction> {

   @Autowired
   private DefaultFieldExtractor salaryAmountExtractor;

    @Autowired
    private DefaultFieldExtractor transferredFundAmountExtractor;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");
    int dateFiledLength = 7;

    public BankAccountProcessor() {
    }

    public void setDateFiledLength(int dateFiledLength) {
        this.dateFiledLength = dateFiledLength;
    }


    public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Transaction process(RawTransaction rawTransaction) throws ParseException {

        Transaction transaction = new Transaction();

        transaction.setDate(extractDate(rawTransaction));
        transaction.setFile(rawTransaction.getFile());

        transaction.setDescription(rawTransaction.getMergedLines());
        transaction.setSalary(Utility.isSalaryTransaction(transaction.getDescription()));

        transaction.setBank(MDC.get(MDCKey.BANK.name()));

        if (transaction.isSalary()) {
            processSalaryTransaction(transaction);
            transaction.setSubHead(Utility.getCompanyName(transaction.getDescription()));
        } else if (Utility.isFamilyTransaction(transaction.getDescription())) {
            if (Utility.isReversalTransaction(transaction.getDescription())) {
                processFamilyTransactionReversal(transaction);
            } else {
                processFamilyTransaction(transaction);

            }
        }

        return transaction;
    }

    protected void processFamilyTransaction(Transaction transaction) {
        String amount = transferredFundAmountExtractor.getField(transaction.getDescription().replaceAll(",", ""));
        try {
            transaction.setCredit(Integer.valueOf(amount));
        } catch (NumberFormatException nfe) {
            log.error("Failed parse transfer amount: {}, Line: {}", amount, transaction.getDescription());
            nfe.printStackTrace();
        }
        transaction.setDebit(0);
        transaction.setHead("Family");
    }

    protected void processFamilyTransactionReversal(Transaction transaction) {
        String amount = transferredFundAmountExtractor.getField(transaction.getDescription().replaceAll(",", ""));
        try {
            transaction.setDebit(Integer.valueOf(amount));
        } catch (NumberFormatException nfe) {
            log.error("Failed parse transfer amount: {}, Line: {}", amount, transaction.getDescription());
            nfe.printStackTrace();
        }
        transaction.setCredit(0);
        transaction.setHead("Family");
    }

    protected void processSalaryTransaction(Transaction transaction) {
        String salary = salaryAmountExtractor.getField(transaction.getDescription().replaceAll(",", ""));
        try {
            transaction.setDebit(Integer.valueOf(salary));
            if (transaction.getDebit() == 0) {
                // Don't consider as Salary
                return;
            }
        } catch (NumberFormatException nfe) {
            log.error("Failed parse salary amount: {}, Line: {}", salary, transaction.getDescription());
            nfe.printStackTrace();
            return;
        }
        log.debug("Salary Amount: {}", transaction.getDebit());
        transaction.setHead("Salary");
        transaction.setCredit(0);
    }

    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,dateFiledLength);
        return simpleDateFormat.parse(dateString);
    }
}


