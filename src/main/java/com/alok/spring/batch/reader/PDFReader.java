package com.alok.spring.batch.reader;

import com.alok.spring.batch.model.RawTransaction;
import com.alok.spring.batch.utils.DefaultLineExtractor;
import com.alok.spring.batch.utils.LineExtractor;
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
    List<RawTransaction> items;
    private int currentIndex = 0;
    private LineExtractor lineExtractor = new DefaultLineExtractor();

    public void setLineExtractor(LineExtractor lineExtractor) {
        this.lineExtractor = lineExtractor;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public RawTransaction read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if (currentIndex < items.size()) {
            return items.get(currentIndex++);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.info("Started Processing File: {}",String.valueOf(resource));
        if (executionContext.containsKey("current.index")) {
            currentIndex = executionContext.getInt("current.index");
        } else {
            currentIndex = 0;
            readLines();
        }
    }

    private void readLines() throws IOException {
        items = new LinkedList<>();
        if (pdfPasssword != null) {
            pdfreader = new PdfReader(resource.getFile().getPath(), pdfPasssword.getBytes());
        } else {
            pdfreader = new PdfReader(resource.getFile().getPath());
        }
        int pages = pdfreader.getNumberOfPages();

        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(pdfreader);

        // Iterate through pages to read content
       // RawTransaction rawTransaction = null;
        for(int i = 1; i <= pages; i++) {
            // Extract content of each page
            String contentOfPage = pdfTextExtractor.getTextFromPage(i, true);
            if (!lineExtractor.extractLine(contentOfPage, items, resource.getFilename())) {
                break;
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt("current.index", currentIndex);
    }

    @Override
    public void close() throws ItemStreamException {
        log.info("Finished Processing File: {}",String.valueOf(resource));
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
