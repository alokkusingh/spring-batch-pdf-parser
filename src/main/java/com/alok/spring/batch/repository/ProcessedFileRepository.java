package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Integer> {
    Optional<List<ProcessedFile>> findAllByName(String valueOf);
    void deleteAllByType(String type);
}
