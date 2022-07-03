package com.alok.spring.repository;

import com.alok.spring.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Integer> {
    List<ProcessedFile> findAllByName(String valueOf);
    @Query(value = "DELETE from  processed_file pf where pf.type = ?1", nativeQuery = true)
    @Modifying
    @Transactional
    void deleteAllByType(String type);
}
