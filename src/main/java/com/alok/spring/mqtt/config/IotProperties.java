package com.alok.spring.mqtt.config;

import com.alok.mqtt.config.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Profile;

@Data
@ConfigurationProperties(prefix = "iot")
@ConfigurationPropertiesScan
@Profile("mqtt")
public class IotProperties extends Properties {

}
