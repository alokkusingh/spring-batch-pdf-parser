package com.alok.spring.batch.processor;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.model.Transaction;
import com.alok.spring.batch.utils.DefaultFieldExtractor;
import com.alok.spring.batch.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    private DefaultFieldExtractor fundTransferAmountExtractor;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy");
    int dateFiledLength = 7;

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

        if (transaction.isSalary()) {
            processSalaryTransaction(transaction);
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
        String amount = fundTransferAmountExtractor.getField(transaction.getDescription().replaceAll(",", ""));
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
        String amount = fundTransferAmountExtractor.getField(transaction.getDescription().replaceAll(",", ""));
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
        } catch (NumberFormatException nfe) {
            log.error("Failed parse salary amount: {}, Line: {}", salary, transaction.getDescription());
            nfe.printStackTrace();
        }
        log.debug("Salary Amount: {}", transaction.getDebit());
        transaction.setCredit(0);
        transaction.setHead("Salary");
    }

    private Date extractDate(RawTransaction rawTransaction) throws ParseException {
        String dateString = rawTransaction.getLines().get(0).substring(0,dateFiledLength);
        return simpleDateFormat.parse(dateString);
    }

    @Bean
    public DefaultFieldExtractor salaryAmountExtractor() {
        DefaultFieldExtractor fieldExtractor = new DefaultFieldExtractor();
        fieldExtractor.setStringPatterns(
                new String[] {
                        // replace comma from pattern to emtpty
                        "(?<=EVOLVING  SYSTEMS)\\d+",
                        "(?<=EVOLVING  SYSTEMS )\\d+",
                        "(?<=EVOLVING SYSTEMS )\\d+",
                        "(?<=EVOLVING  SYSTEMS  NETWORKS  INDIA  PVT LTD )\\d+",
                        "(?<=EVOLVING  SYSTEMS  NETWORKS  I  PVT  LTD)\\d+",
                        "(?<=EVOLING  SYSTEMS  NETWORKS  INDIA  PVT LTD )\\d+",
                        "(?<=NEFT  INWARD)\\d+",
                        "(?<=TRF  FROM EVOLVING )\\d+",
                        "(?<=NEFTINW-[0-9]{10,13} )\\d+",
                }
        );
        return fieldExtractor;
    }

    @Bean
    public DefaultFieldExtractor fundTransferAmountExtractor() {
       DefaultFieldExtractor fieldExtractor = new DefaultFieldExtractor();
       fieldExtractor.setStringPatterns(
               new String[] {
                       "(?<=TO)\\d+",
                       "(?<=OUTWARD  ORG)\\d+",
                       "(?<=IMPS-[0-9]{10,15} )\\d+",
                       "(?<=UPI-[0-9]{10,15} )\\d+",
                       "(?<=EBPP-[0-9]{10,15} )\\d+",
                       "(?<=OUTWARD  REV)\\d+",
               }
       );

       return fieldExtractor;
    }
}


