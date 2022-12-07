package com.alok.spring.mqtt.config;

import com.alok.mqtt.listener.MqttCallbackListener;
import com.alok.mqtt.processor.RequestProcessor;
import com.alok.mqtt.service.MqttClientService;
import com.alok.spring.config.ServerProperties;
import com.alok.spring.mqtt.listener.CustomMqttCallbackListener;
import com.alok.spring.mqtt.processor.CustomRequestProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("mqtt")
@Configuration
public class MqttClientConfig {


    private IotProperties iotProperties;
    private ServerProperties serverProperties;

    //@Autowired
    //private MqttClientService mqttClientService;

    public MqttClientConfig(IotProperties iotProperties, ServerProperties serverProperties) {
        this.iotProperties = iotProperties;
        this.serverProperties = serverProperties;
    }


    @Bean(initMethod = "connect", destroyMethod = "disConnect")
    public MqttClientService mqttClientService() {

        MqttClientService mqttClientService = new MqttClientService();

        mqttClientService.setIotProperties(iotProperties);
        mqttClientService.setMqttCallbackListener(mqttCallbackListener());

        return mqttClientService;
    }


    public MqttCallbackListener mqttCallbackListener() {
        return new CustomMqttCallbackListener(
                requestProcessor()
        );
    }

    @Bean
    public RequestProcessor requestProcessor() {
        return new CustomRequestProcessor(
                objectMapper(),
                restTemplate(),
                serverProperties.getPort()
        );
    }

    // Don't decalare ObjectMapper as bean
    // Side effect - the actuall PDF API implementaion uses this bean and converts time to epoche
    //@Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
