package com.alok.spring.batch.repository;

import com.alok.spring.batch.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Integer> {
    Optional<List<ProcessedFile>> findAllByName(String valueOf);
    @Query(value = "DELETE from  processed_file pf where pf.type = ?1", nativeQuery = true)
    @Modifying
    void deleteAllByType(String type);
}
