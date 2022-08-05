package com.alok.spring.mqtt.payload;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
@Builder
public class ResponsePayload {

    private Integer code;
    private String body;
    private HttpHeaders httpHeaders;
    private String correlationId;

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"correlationId\":\"" + correlationId + "\"" +
                ", \"body\":" + body  +
                ", \"httpHeaders\":" + httpHeaders +
                "}";
    }
}
