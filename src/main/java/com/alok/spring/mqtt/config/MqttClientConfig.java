package com.alok.spring.mqtt.config;

import com.alok.mqtt.listener.MqttCallbackListener;
import com.alok.mqtt.processor.RequestProcessor;
import com.alok.mqtt.service.MqttClientService;
import com.alok.mqtt.utils.CertUtils;
import com.alok.spring.config.ServerProperties;
import com.alok.spring.mqtt.processor.CustomRequestProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Profile("mqtt")
@Configuration
public class MqttClientConfig {


    @Autowired
    private IotProperties iotProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private MqttClientService mqttClientService;


    @Bean
    public MqttConnectOptions mqttClientConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(iotProperties.getMqtt().getCleanState());
        mqttConnectOptions.setAutomaticReconnect(iotProperties.getMqtt().getAutoReconnect());
        mqttConnectOptions.setConnectionTimeout(iotProperties.getMqtt().getConnectionTimeout());
        mqttConnectOptions.setKeepAliveInterval(iotProperties.getMqtt().getKeepAlive());
        mqttConnectOptions.setWill(iotProperties.getMqtt().getStatusTopic(), offlineMessage(), 1, false);

        mqttConnectOptions.setSSLProperties(sslClientProperties());

        return mqttConnectOptions;
    }

    @Bean
    public Properties sslClientProperties() {

        //valid properties are {"com.ibm.ssl.protocol", "com.ibm.ssl.contextProvider", "com.ibm.ssl.keyStore", "com.ibm.ssl.keyStorePassword", "com.ibm.ssl.keyStoreType", "com.ibm.ssl.keyStoreProvider", "com.ibm.ssl.keyManager", "com.ibm.ssl.trustStore", "com.ibm.ssl.trustStorePassword", "com.ibm.ssl.trustStoreType", "com.ibm.ssl.trustStoreProvider", "com.ibm.ssl.trustManager", "com.ibm.ssl.enabledCipherSuites", "com.ibm.ssl.clientAuthentication"};
        Properties properties = new Properties();
        properties.setProperty("com.ibm.ssl.keyStoreType", iotProperties.getSecure().getKeystoreType());
        properties.setProperty("com.ibm.ssl.keyStore", CertUtils.getClientKeyStore(iotProperties.getSecure().getKeystoreFile()));
        properties.setProperty("com.ibm.ssl.keyStorePassword", iotProperties.getSecure().getKeystorePassword());
        properties.setProperty("com.ibm.ssl.trustStore", CertUtils.getClientTrustStore(iotProperties.getSecure().getTruststoreFile()));
        properties.setProperty("com.ibm.ssl.trustStorePassword", iotProperties.getSecure().getTruststorePassword());
        return properties;
    }

    @Bean(initMethod = "connect", destroyMethod = "disConnect")
    public MqttClientService mqttClientService(
            MqttConnectOptions mqttClientConnectOptions
    ) {

        MqttClientService mqttClientService = new MqttClientService();

        mqttClientService.setIotProperties(iotProperties);
        mqttClientService.setMqttConnectOptions(mqttClientConnectOptions);
        mqttClientService.setMqttCallbackListener(mqttCallbackListener());

        return mqttClientService;
    }


    public MqttCallbackListener mqttCallbackListener() {
        return MqttCallbackListener.builder()
                .requestProcessor(requestProcessor())
                .build();
    }

    @Bean
    public RequestProcessor requestProcessor() {
        return new CustomRequestProcessor(
                objectMapper(),
                restTemplate(),
                serverProperties.getPort()
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public byte[] offlineMessage() {

        return (iotProperties.getMqtt().getClientId() + "-OFFLINE").getBytes(StandardCharsets.UTF_8);
    }
}
