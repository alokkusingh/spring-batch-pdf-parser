package com.alok.spring.response;

import com.alok.spring.constant.UploadType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadFileResponse {
        private String fileName;
        private String fileType;
        private long size;
        private String message;
        private String fileDownloadUri;
        private UploadType uploadType;
}
