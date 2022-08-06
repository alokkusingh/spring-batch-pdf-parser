package com.alok.spring.mqtt.config;

import com.alok.spring.mqtt.payload.RequestPayload;
import com.alok.spring.mqtt.payload.ResponsePayload;
import com.alok.spring.mqtt.service.MqttClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Profile("mqtt")
@Component
public class MqttCallbackListener implements MqttCallback {

    @Autowired
    private MqttClientService mqttClientService;

    @Autowired
    private ObjectMapper objectMapper;

   @Autowired
   private RestTemplate restTemplate;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private IotProperties iotProperties;

    @Override
    public void connectionLost(Throwable throwable) {
        log.warn("Connection Lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        log.info("Message arrived - {} - {}", topic, mqttMessage);

        RequestPayload requestPayload = null;
        try {
            requestPayload = objectMapper.readValue(mqttMessage.getPayload(), RequestPayload.class);
        } catch (Exception rte) {
            rte.printStackTrace();
            log.error("Failed to parse MQTT Request, error: {}", rte.getMessage());
        }

        // 1. Call REST API
        ResponseEntity<String> httpResponse = null;
        if (requestPayload != null) {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
            httpResponse = restTemplate.exchange("http://localhost:" + serverPort + requestPayload.getUri(), requestPayload.getHttpMethod(), requestEntity, String.class);
        }
        httpResponse = Optional.ofNullable(httpResponse).orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        requestPayload = Optional.ofNullable(requestPayload).orElse(new RequestPayload());

        // 2. Send response to MQTT broker
        ResponsePayload responsePayload = ResponsePayload.builder()
                .code(httpResponse.getStatusCodeValue())
                .body(httpResponse.getBody())
                .correlationId(requestPayload.getCorrelationId())
                .build();
        MqttMessage mqttResponseMessage = new MqttMessage();
        mqttResponseMessage.setQos(1);
        // AWS IoT Core doesn't support retained=true
        mqttResponseMessage.setRetained(false);
        mqttResponseMessage.setPayload(responsePayload.toString().getBytes(StandardCharsets.UTF_8));
        mqttClientService.publish(mqttResponseMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        log.info("Delivery completed - {}", iMqttDeliveryToken.getMessageId());

    }
}
