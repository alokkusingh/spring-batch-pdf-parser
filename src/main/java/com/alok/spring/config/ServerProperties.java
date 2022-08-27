package com.alok.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationProperties(prefix = "server")
@ConfigurationPropertiesScan
public class ServerProperties {
    private Integer port;
}
