package com.alok.spring.batch.utils;

import com.alok.spring.batch.model.RawTransaction;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class PDFReader implements ResourceAwareItemReaderItemStream {
    private Resource resource;
    private String pdfPasssword;
    private PdfReader pdfreader;
    private String startReadingText;
    private String[] linesToSkip;

    public void setLinesToSkip(String[] linesToSkip) {
        this.linesToSkip = linesToSkip;
    }

    public void setStartReadingText(String startReadingText) {
        this.startReadingText = startReadingText;
    }

    public void setEndReadingText(String endReadingText) {
        this.endReadingText = endReadingText;
    }

    private String endReadingText;

    List<RawTransaction> items;
    private int currentIndex = 0;

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public RawTransaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if (currentIndex < items.size()) {
            //log.debug("Alok line: {}", items.get(currentIndex));
            return items.get(currentIndex++);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.debug(String.valueOf(resource));
        //log.debug(pdfPasssword);
        //log.debug(String.valueOf(executionContext.containsKey("current.index")));
        if (executionContext.containsKey("current.index")) {
            currentIndex = executionContext.getInt("current.index");
        } else {
            currentIndex = 0;
            readLines();
        }
    }

    private void readLines() throws IOException {
        items = new LinkedList<>();
        //pdfreader = new PdfReader(resource.getURL().getPath(), pdfPasssword.getBytes());
        pdfreader = new PdfReader(resource.getFile().getPath(), pdfPasssword.getBytes());
        int pages = pdfreader.getNumberOfPages();

        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(pdfreader);

        boolean start = false;
        if (startReadingText == null)
            start = true;

        boolean stop = false;
        // Iterate through pages to read content
        RawTransaction rawTransaction = null;
        for(int i = 1; i <= pages && !stop; i++) {
            // Extract content of each page
            String contentOfPage = pdfTextExtractor.getTextFromPage(i, true);
            //Arrays.stream(contentOfPage.split("\\r?\\n"))
            //        .map(str -> str.trim())
            //        .filter(str -> !str.isEmpty())
            //        .forEach(item -> items.add(item));
            for (String line: contentOfPage.split("\\r?\\n")) {
               if (shouldSkip(line))
                    continue;

               if (!start) {
                    if (line.matches(startReadingText)) {
                        log.debug("Starting line processing after this line: {}", line);
                        start = true;
                    }
               } else {
                   if (endReadingText != null && line.matches(endReadingText)) {
                       log.debug("Stopping line processing from this line: {}", line);
                       stop = true;
                       break;
                   }
                   //items.add(line);
                   if (line.matches("^[0-9]{2}[a-zA-Z]{3}[0-9]{2}.*")) {
                       //if (rawTransaction != null)
                       //    items.add(rawTransaction);
                       rawTransaction = new RawTransaction();
                       items.add(rawTransaction);
                   }
                   if (rawTransaction != null && !line.matches(startReadingText))
                       rawTransaction.getLines().add(line);
               }
            }
        }
    }

    private boolean shouldSkip(String line) {
        if (line.trim().isEmpty())
            return true;

        if (linesToSkip != null) {
           for (String skipLine: linesToSkip) {
               if (line.matches(skipLine)) {
                   return true;
               }
           }
        }

        return false;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        //log.debug("Alok update");
        executionContext.putInt("current.index", currentIndex);
    }

    @Override
    public void close() throws ItemStreamException {
        if(pdfreader != null) {
            pdfreader.close();
        }
    }

    public void setFilePassword(String password) {
        this.pdfPasssword = password;
    }

    public void setName(String s) {
    }
}
