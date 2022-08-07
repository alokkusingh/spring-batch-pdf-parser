package com.alok.spring.mqtt.listener;

import com.alok.mqtt.listener.MqttCallbackListener;
import com.alok.mqtt.payload.RequestPayload;
import com.alok.mqtt.payload.ResponsePayload;
import com.alok.mqtt.processor.RequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class CustomMqttCallbackListener extends MqttCallbackListener {

    private RequestProcessor requestProcessor;

    public CustomMqttCallbackListener() {
        super();
    }
    public CustomMqttCallbackListener( RequestProcessor requestProcessor ) {
        this();
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        log.info("Message arrived - {} - {}", topic, mqttMessage);
        try {
            RequestPayload requestPayload = this.requestProcessor.parseMqttRequest(mqttMessage.getPayload());
            ResponseEntity<String> httpResponse = this.requestProcessor.processRequest(requestPayload);
            httpResponse = Optional.ofNullable(httpResponse).orElse(new ResponseEntity(HttpStatus.BAD_REQUEST));
            requestPayload = Optional.ofNullable(requestPayload).orElse(new RequestPayload());
            ResponsePayload responsePayload = this.requestProcessor.prepareMqttResponse(requestPayload, httpResponse);
            super.getMqttClientService().publish(responsePayload);
        } catch (Exception e) {
            log.error("Error processing payload, error: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
