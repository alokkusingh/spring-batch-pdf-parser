package com.alok.spring.service;

import com.alok.spring.batch.utils.Utility;
import com.alok.spring.constant.InvestmentType;
import com.alok.spring.model.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleSheetService {

    private String serviceAccountKeyFile;
    private String expenseSheetId;
    private String taxSheetRange;
    private String expenseSheetRange;
    private String investmentSheetRange;
    private Sheets sheetsService;
    private String odionSheetId;
    private String odionTransactionsSheetRange;
    private TaxService taxService;
    private ExpenseService expenseService;
    private InvestmentService investmentService;
    private CacheService cacheService;
    private OdionService odionService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    public GoogleSheetService(
            @Value("${file.path.service_account.key}") String serviceAccountKeyFile,
            @Value("${sheet.id.expense}") String expenseSheetId,
            @Value("${range.tax-sheet}") String taxSheetRange,
            @Value("${range.expense-sheet}") String expenseSheetRange,
            @Value("${range.investment-sheet}") String investmentSheetRange,
            @Value("${sheet.id.odion}") String odionSheetId,
            @Value("${range.odion.transaction}") String odionTransactionsSheetRange,
            TaxService taxService,
            ExpenseService expenseService,
            InvestmentService investmentService,
            CacheService cacheService,
            OdionService odionService
    ) throws IOException, GeneralSecurityException {
        this.serviceAccountKeyFile = serviceAccountKeyFile;
        this.expenseSheetId = expenseSheetId;
        this.taxSheetRange = taxSheetRange;
        this.expenseSheetRange = expenseSheetRange;
        this.investmentSheetRange = investmentSheetRange;
        this.odionSheetId = odionSheetId;
        this.odionTransactionsSheetRange = odionTransactionsSheetRange;
        this.taxService = taxService;
        this.expenseService = expenseService;
        this.investmentService = investmentService;
        this.cacheService = cacheService;
        this.odionService = odionService;

        InputStream inputStream = new FileInputStream(serviceAccountKeyFile); // put your service account's key.json file in asset folder.

        GoogleCredential googleCredential = GoogleCredential.fromStream(inputStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                googleCredential
        )
                .setApplicationName("Home Stack")
                .build();
    }

    public void refreshTaxData() throws IOException {

        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, taxSheetRange)
                .execute();

        List<Tax> taxRecords = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .map(row -> Tax.builder()
                        .financialYear((String) row.get(0))
                        .paidAmount(row.size() < 2? 0: Integer.parseInt((String) row.get(1)))
                        .refundAmount(row.size() < 3? 0: Integer.parseInt((String) row.get(2)))
                        .build()
                )
                .toList();

        taxService.saveAllTaxes(taxRecords);
        cacheService.evictAllCaches();
    }

    public void refreshExpenseData() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, expenseSheetRange)
                .execute();

        List<Expense> expenseRecords = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .filter(row -> row.get(2) != null && row.get(2) != "")
                .map(row -> Expense.builder()
                        .date(parseToDate((String) row.get(0)))
                        .head((String) row.get(1))
                        .amount(Double.parseDouble((String) row.get(2)))
                        .comment(row.get(3) == null? "": (String) row.get(3))
                        .yearx(Integer.parseInt((String) row.get(4)))
                        .monthx(Integer.parseInt((String) row.get(5)))
                        .category(Utility.getExpenseCategory((String) row.get(1), row.get(3) == null? "": (String) row.get(3)))
                        .build()
                )
                .toList();

        expenseService.saveAllExpenses(expenseRecords);
        cacheService.evictAllCaches();
    }

    public void refreshInvestmentData() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, investmentSheetRange)
                .execute();

        List<Investment> investmentRecords = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .map(row -> List.of(
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.PF.name())
                                        .contribution(row.get(2) == "" ? 0 : Integer.parseInt((String) row.get(2)))
                                        .valueAsOnMonth(row.get(4) == "" ? 0 : Integer.parseInt((String) row.get(4)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.LIC.name())
                                        .contribution(row.get(5) == "" ? 0 : Integer.parseInt((String) row.get(5)))
                                        .valueAsOnMonth(row.get(7) == "" ? 0 : Integer.parseInt((String) row.get(7)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.NPS.name())
                                        .contribution(row.get(8) == "" ? 0 : Integer.parseInt((String) row.get(8)))
                                        .valueAsOnMonth(row.get(10) == "" ? 0 : Integer.parseInt((String) row.get(10)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.SHARE.name())
                                        .contribution(row.get(11) == "" ? 0 : Integer.parseInt((String) row.get(11)))
                                        .valueAsOnMonth(row.get(13) == "" ? 0 : Integer.parseInt((String) row.get(13)))
                                        .build()
                        )
                )
                .flatMap(Collection::stream)
                .toList();

        investmentService.saveAllInvestments(investmentRecords);
        cacheService.evictAllCaches();
    }

    public void refreshOdionTransactionsData() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(odionSheetId, odionTransactionsSheetRange)
                .execute();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<OdionTransaction> odionTransactionRecords = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .filter(row -> row.size() == 5)
                .filter(row -> row.get(2) != "")
                .filter(row -> row.get(3) != "")
                .map(row -> OdionTransaction.builder()
                        .date(LocalDate.parse((String)row.get(0), formatter))
                        .particular((String)row.get(1))
                        .debitAccount(row.get(2) == "" ? OdionTransaction.Account.OPENING_BALANCE : OdionTransaction.Account.valueOfOrDefault((String)row.get(2)))
                        .creditAccount(OdionTransaction.Account.valueOfOrDefault((String)row.get(3)))
                        .amount(Double.parseDouble((String) row.get(4)))
                        .build()
                )
                .toList();

        odionService.saveAllTransactions(odionTransactionRecords);
    }

    private Date parseToDate(String strDate) {
        try {
            return simpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}