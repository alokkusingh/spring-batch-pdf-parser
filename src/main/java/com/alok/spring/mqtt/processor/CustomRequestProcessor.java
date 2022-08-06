package com.alok.spring.mqtt.processor;

import com.alok.mqtt.payload.RequestPayload;
import com.alok.mqtt.processor.RequestProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Data
public class CustomRequestProcessor extends RequestProcessor {

    private RestTemplate restTemplate;
    private Integer serverPort;

    public CustomRequestProcessor(
            ObjectMapper objectMapper,
            RestTemplate restTemplate,
            Integer serverPort
    ) {
        super(objectMapper);
        this.restTemplate = restTemplate;
        this.serverPort = serverPort;
    }

    @Override
    public ResponseEntity<String> processRequest(RequestPayload requestPayload) {
        ResponseEntity<String> httpResponse = null;
        if (requestPayload != null) {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
            httpResponse = restTemplate.exchange("http://localhost:" + serverPort + requestPayload.getUri(), requestPayload.getHttpMethod(), requestEntity, String.class);
        } else {
            requestPayload = new RequestPayload();
            httpResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return httpResponse;
    }

}
