package com.alok.spring.mqtt.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpMethod;

@ToString
@Data
@NoArgsConstructor
public class RequestPayload  {

    private HttpMethod httpMethod;
    private String uri;
    private String body;
    private String correlationId;

}
