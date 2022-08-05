package com.alok.spring.mqtt.config;

import com.alok.spring.mqtt.service.MqttClientService;
import com.alok.spring.mqtt.utils.CertUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Profile("mqtt")
@Configuration
public class MqttClientConfig {

    @Value("${iot.mqtt.host}")
    private String iotHost;

    @Value("${iot.mqtt.port}")
    private String iotPort;

    @Value("${iot.client.id}")
    private String clientId;

    @Autowired
    private MqttCallback mqttCallbackListener;

    @Value("${iot.status.topic}")
    private String willTopic;

    public byte[] offlineMessage() {

        return (clientId + "-OFFLINE").getBytes(StandardCharsets.UTF_8);
    }

    public byte[] onlineMessage() {
        return (clientId + "-ONLINE").getBytes(StandardCharsets.UTF_8);
    }

    @Bean
    public IMqttClient mqttClient() throws MqttException {

        String publisherId = clientId;
        String iotClientUrl = String.format("ssl://%s:%s", iotHost, iotPort);

        MqttClient mqttClient = new MqttClient(iotClientUrl, publisherId);

        mqttClient.setCallback(mqttCallbackListener);

        return mqttClient;
    }

    @Bean
    public MqttConnectOptions mqttClientConnectOptions(
            @Value("${iot.mqtt.clean-state}") Boolean cleanState,
            @Value("${iot.mqtt.auto-reconnect}") Boolean autoReconnect,
            @Value("${iot.mqtt.conn-timeout}") Integer connTimeout,
            @Value("${iot.mqtt.keep-alive}") Integer keepAliveTime,
            Properties sslClientProperties
    ) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(cleanState);
        mqttConnectOptions.setAutomaticReconnect(autoReconnect);
        mqttConnectOptions.setConnectionTimeout(connTimeout);
        mqttConnectOptions.setKeepAliveInterval(keepAliveTime);
        mqttConnectOptions.setWill(willTopic, offlineMessage(), 1, false);

        mqttConnectOptions.setSSLProperties(sslClientProperties);

        return mqttConnectOptions;
    }

    @Bean
    public Properties sslClientProperties(
            @Value("${iot.keystore.type}") String keystoreType,
            @Value("${iot.keystore.file}") String keystoreFile,
            @Value("${iot.truststore.file}") String truststoreFile,
            @Value("${iot.keystore.password}") String keystorePassword,
            @Value("${iot.truststore.password}") String truststorePassword
    ) {

        //valid properties are {"com.ibm.ssl.protocol", "com.ibm.ssl.contextProvider", "com.ibm.ssl.keyStore", "com.ibm.ssl.keyStorePassword", "com.ibm.ssl.keyStoreType", "com.ibm.ssl.keyStoreProvider", "com.ibm.ssl.keyManager", "com.ibm.ssl.trustStore", "com.ibm.ssl.trustStorePassword", "com.ibm.ssl.trustStoreType", "com.ibm.ssl.trustStoreProvider", "com.ibm.ssl.trustManager", "com.ibm.ssl.enabledCipherSuites", "com.ibm.ssl.clientAuthentication"};
        Properties properties = new Properties();
        properties.setProperty("com.ibm.ssl.keyStoreType", keystoreType);
        properties.setProperty("com.ibm.ssl.keyStore", CertUtils.getClientKeyStore(keystoreFile));
        properties.setProperty("com.ibm.ssl.keyStorePassword", keystorePassword);
        properties.setProperty("com.ibm.ssl.trustStore", CertUtils.getClientTrustStore(truststoreFile));
        properties.setProperty("com.ibm.ssl.trustStorePassword", truststorePassword);
        return properties;
    }

    @Bean(initMethod = "connect", destroyMethod = "disConnect")
    public MqttClientService mqttClientService(
            IMqttClient mqttClient,
            MqttConnectOptions mqttClientConnectOptions,
            @Value("${iot.mqtt-connection-retry}") Integer connectRetry,
            @Value("${iot.subscribe.topic}") String subscriptionTopic,
            @Value("${iot.publish.topic}") String publishTopic,
            @Value("${iot.subscribe.qos}") Integer qos
    ) {

        MqttClientService mqttClientService = new MqttClientService();

        mqttClientService.setClientId(clientId);
        mqttClientService.setMqttClient(mqttClient);
        mqttClientService.setMqttConnectOptions(mqttClientConnectOptions);
        mqttClientService.setConnectRetry(connectRetry);
        mqttClientService.setSubscriptionTopic(subscriptionTopic);
        mqttClientService.setQos(qos);
        mqttClientService.setPublishTopic(publishTopic);

        return mqttClientService;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
