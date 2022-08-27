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
        RequestPayload requestPayload = null;
        ResponseEntity<String> httpResponse = null;
        try {
            requestPayload = this.requestProcessor.parseMqttRequest(mqttMessage.getPayload());

            httpResponse = this.requestProcessor.processRequest(requestPayload);
        } catch (Exception e) {
            log.error("Error processing payload, error: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            requestPayload = Optional.ofNullable(requestPayload).orElse(new RequestPayload());
            httpResponse = Optional.ofNullable(httpResponse).orElse(new ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST));
            ResponsePayload responsePayload = this.requestProcessor.prepareMqttResponse(requestPayload, httpResponse);
            try {
                super.getMqttClientService().publish(responsePayload);
            } catch (Exception e) {
                // Dont throw otherwise the request wll payload will be received again
                e.printStackTrace();
            }
        }
    }
}
