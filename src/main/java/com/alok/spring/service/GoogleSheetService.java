package com.alok.spring.service;

import com.alok.spring.batch.utils.Utility;
import com.alok.spring.constant.InvestmentType;
import com.alok.spring.model.Expense;
import com.alok.spring.model.Investment;
import com.alok.spring.model.OdionTransaction;
import com.alok.spring.model.Tax;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
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

//        InputStream inputStream = new FileInputStream(serviceAccountKeyFile); // put your service account's key.json file in asset folder.
//
//
//        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream)
//                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
//
//        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);
//
//        sheetsService = new Sheets.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                GsonFactory.getDefaultInstance(),
//                requestInitializer
//        )
//                .setApplicationName("Home Stack")
//                .build();
    }

    private void initSheetService() {
        if (sheetsService == null) {
            log.info("Google Sheet Service Initialized!");
            InputStream inputStream = null; // put your service account's key.json file in asset folder.
            try {
                inputStream = new FileInputStream(serviceAccountKeyFile);
                //byte[] bytes = inputStream.readAllBytes();
                //log.info("Content: {}", new String(bytes));
            } catch (FileNotFoundException e) {
                log.error("Google Sheet initialization failed with error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                log.error("Google Sheet initialization stream read failed: " + e.getMessage());
                throw new RuntimeException(e);
            }


            GoogleCredentials googleCredentials = null;
            try {
                googleCredentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
            } catch (IOException e) {
                log.error("Google Sheet initialization failed with error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);

            try {
                sheetsService = new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        //GsonFactory.getDefaultInstance(),
                        JacksonFactory.getDefaultInstance(),
                        requestInitializer
                )
                        .setApplicationName("Home Stack")
                        .build();
            } catch (GeneralSecurityException | IOException | RuntimeException e) {
                log.error("Google Sheet initialization failed with error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void refreshTaxData() throws IOException {
        initSheetService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, taxSheetRange)
                .execute();

        List<Tax> records = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .map(row -> Tax.builder()
                        .financialYear((String) row.get(0))
                        .paidAmount(row.size() < 2? 0: Integer.parseInt((String) row.get(1)))
                        .refundAmount(row.size() < 3? 0: Integer.parseInt((String) row.get(2)))
                        .build()
                )
                .toList();

        log.info("Number of transactions: {}", records.size());
        taxService.saveAllTaxes(records);
        cacheService.evictAllCaches();
    }

    public void refreshExpenseData() throws IOException {
        initSheetService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, expenseSheetRange)
                .execute();

        List<Expense> records = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .filter(row -> row.get(2) != null && ((String) row.get(2)).length() != 0)
                .map(row -> Expense.builder()
                        .date(parseToDate((String) row.get(0)))
                        .head((String) row.get(1))
                        .amount(Double.parseDouble((String) row.get(2)))
                        .comment(row.get(3) == null? "": (String) row.get(3))
                        .yearx(row.get(4) == null? 0:Integer.parseInt((String) row.get(4)))
                        .monthx(row.get(5) == null? 0:Integer.parseInt((String) row.get(5)))
                        .category(Utility.getExpenseCategory((String) row.get(1), row.get(3) == null? "": (String) row.get(3)))
                        .build()
                )
                .toList();

        log.info("Number of transactions: {}", records.size());

        expenseService.saveAllExpenses(records);
        cacheService.evictAllCaches();
    }

    public void refreshInvestmentData() throws IOException {
        initSheetService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(expenseSheetId, investmentSheetRange)
                .execute();

        List<Investment> records = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .map(row -> List.of(
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.PF.name())
                                        .contribution(((String) row.get(2)).length() != 0 ? 0 : Integer.parseInt((String) row.get(2)))
                                        .valueAsOnMonth(((String) row.get(4)).length() != 0 ? 0 : Integer.parseInt((String) row.get(4)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.LIC.name())
                                        .contribution(((String) row.get(5)).length() != 0 ? 0 : Integer.parseInt((String) row.get(5)))
                                        .valueAsOnMonth(((String) row.get(7)).length() != 0 ? 0 : Integer.parseInt((String) row.get(7)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.NPS.name())
                                        .contribution(((String) row.get(8)).length() != 0 ? 0 : Integer.parseInt((String) row.get(8)))
                                        .valueAsOnMonth(((String) row.get(10)).length() != 0 ? 0 : Integer.parseInt((String) row.get(10)))
                                        .build(),
                                Investment.builder()
                                        .yearx(Short.parseShort((String) row.get(0)))
                                        .monthx(Short.parseShort((String) row.get(1)))
                                        .head(InvestmentType.SHARE.name())
                                        .contribution(((String) row.get(11)).length() != 0 ? 0 : Integer.parseInt((String) row.get(11)))
                                        .valueAsOnMonth(((String) row.get(13)).length() != 0 ? 0 : Integer.parseInt((String) row.get(13)))
                                        .build()
                        )
                )
                .flatMap(Collection::stream)
                .toList();

        log.info("Number of transactions: {}", records.size());
        investmentService.saveAllInvestments(records);
        cacheService.evictAllCaches();
    }

    public void refreshOdionTransactionsData() throws IOException {
        initSheetService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(odionSheetId, odionTransactionsSheetRange)
                .execute();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<OdionTransaction> records = Optional.ofNullable(response.getValues()).orElse(Collections.emptyList()).stream()
                .filter(row -> row.size() == 5)
                .filter(row -> ((String) row.get(2)).length() != 0)
                .filter(row -> ((String) row.get(3)).length() != 0)
                .map(row -> OdionTransaction.builder()
                        .date(LocalDate.parse((String)row.get(0), formatter))
                        .particular((String)row.get(1))
                        .debitAccount(((String) row.get(2)).length() != 0 ? OdionTransaction.Account.OPENING_BALANCE : OdionTransaction.Account.valueOfOrDefault((String)row.get(2)))
                        .creditAccount(OdionTransaction.Account.valueOfOrDefault((String)row.get(3)))
                        .amount(Double.parseDouble((String) row.get(4)))
                        .build()
                )
                .toList();

        log.info("Number of transactions: {}", records.size());
        odionService.saveAllTransactions(records);
    }

    private Date parseToDate(String strDate) {
        try {
            return simpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}