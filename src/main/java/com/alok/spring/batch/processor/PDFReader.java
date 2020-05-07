package com.alok.spring.batch.processor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PDFReader<T> implements ResourceAwareItemReaderItemStream<T> {
    private Resource resource;
    private String pdfPasssword;
    private PdfReader pdfreader = null;

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        int pages = pdfreader.getNumberOfPages();
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(pdfreader);
        // Iterate through pages to read content
        for(int i = 1; i <= pages; i++) {
            // Extract content of each page
            String contentOfPage = pdfTextExtractor.getTextFromPage(i, true);
            System.out.println(contentOfPage );
            log.debug(contentOfPage);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.debug(String.valueOf(resource));
        for (Map.Entry entry :executionContext.entrySet())
            log.debug("Entry key: {}", entry.getKey());
        log.debug(pdfPasssword);
        pdfreader = new PdfReader(resource.getURL().getPath(), pdfPasssword.getBytes());
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

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

    public void setName(T s) {
    }
}
