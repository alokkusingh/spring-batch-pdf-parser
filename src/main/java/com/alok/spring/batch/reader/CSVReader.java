package com.alok.spring.batch.reader;

import com.alok.spring.batch.model.ProcessedFile;
import com.alok.spring.batch.repository.ProcessedFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CSVReader<T> extends FlatFileItemReader<T> {

    private Resource resource;
    private String transactionType;
    private ProcessedFileRepository processedFileRepository;

    public CSVReader(ProcessedFileRepository processedFileRepository, LineMapper<T> lineMapper) {
        this.processedFileRepository = processedFileRepository;
        setLineMapper(lineMapper);
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.info("Started Processing File: {}", String.valueOf(resource));
        Optional<List<ProcessedFile>> processedFile = processedFileRepository.findAllByName(String.valueOf(resource));
        if (processedFile.isPresent()) {
            log.warn("File already processed - skipping!");
            return;
        }
        super.open(executionContext);
    }

    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
        this.resource = resource;
    }

    @Override
    public void close() throws ItemStreamException {
        super.close();
        if (resource != null) {
            log.debug("Finished Processing File: {}", String.valueOf(resource));
            Optional<List<ProcessedFile>> processedFile = processedFileRepository.findAllByName(String.valueOf(resource));
            if (!processedFile.isPresent()) {
                processedFileRepository.save(
                        ProcessedFile.builder()
                                .name(String.valueOf(resource))
                                .date(new Date())
                                .type(transactionType)
                                .build()
                );
            }
        }
    }

}
