package com.alok.spring.service;

import com.alok.spring.exception.FileStorageException;
import com.alok.spring.constant.UploadType;
import com.alok.spring.exception.UploadTypeNotSupportedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${dir.path.kotak_account.imported}")
    private String kotakImportedLocation;

    @Value("${dir.path.hdfc_account.imported}")
    private String hdfcImportedLocation;

    @Value("${dir.path.expense}")
    private String expenseDirLocation;

    @Value("${dir.path.tax}")
    private String taxDirLocation;

    @Value("${dir.path.investment}")
    private String investmentDirLocation;

    private Path getStoragePath(UploadType uploadType) {
        return switch(uploadType) {
            case KotakExportedStatement -> Paths.get(kotakImportedLocation).toAbsolutePath().normalize() ;
            case HDFCExportedStatement -> Paths.get(hdfcImportedLocation).toAbsolutePath().normalize();
            case ExpenseGoogleSheet -> Paths.get(expenseDirLocation).toAbsolutePath().normalize();
            case TaxGoogleSheet -> Paths.get(taxDirLocation).toAbsolutePath().normalize();
            case InvestmentGoogleSheet -> Paths.get(investmentDirLocation).toAbsolutePath().normalize();
            default -> throw new UploadTypeNotSupportedException(String.format("Upload Type %s not supported", uploadType.name()));
        };
    }

    private String getUploadFileName(UploadType uploadType, String fileName) {
        return switch(uploadType) {
            case HDFCExportedStatement, KotakExportedStatement -> fileName;
            case ExpenseGoogleSheet -> StringUtils.cleanPath("Expense Sheet - Form Responses 1.csv");
            case TaxGoogleSheet -> StringUtils.cleanPath("Expense Sheet - Tax by year.csv");
            case InvestmentGoogleSheet -> StringUtils.cleanPath("Expense Sheet - Investment.csv");
            default -> throw new UploadTypeNotSupportedException(String.format("Upload Type %s not supported", uploadType.name()));
        };
    }

    public String storeFile(MultipartFile file, UploadType uploadType) {
        // Normalize file name
        String fileName = getUploadFileName(uploadType, StringUtils.cleanPath(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path storageLocationPath = getStoragePath(uploadType);

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = storageLocationPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
