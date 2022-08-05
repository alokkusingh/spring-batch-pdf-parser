package com.alok.spring.mqtt.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Slf4j
@Data
public class MqttClientService {

    private String clientId;
    private IMqttClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;
    private Integer connectRetry;
    private String subscriptionTopic;

    private String publishTopic;
    private Integer qos;

    public void connect() {
        boolean connected = false;
        int retryCount = 0;
        while (!connected && retryCount < connectRetry) {
            try {
                log.warn("[{}] Connecting to MQTT broker - attempt: {}", clientId, retryCount + 1);
                log.debug(mqttConnectOptions.toString());
                mqttClient.connect(mqttConnectOptions);
                log.warn("[{}] Connected: {}", clientId, mqttClient.isConnected());
                mqttClient.subscribe(new String[] {subscriptionTopic}, new int[] {qos});
                log.warn("[{}] Subscribed: {}", clientId, subscriptionTopic);

                connected = true;
            } catch (MqttException e) {
                e.printStackTrace();
                ++retryCount;
                if (retryCount < connectRetry) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }
    }

    public void disConnect() {
        try {
            log.warn("[{}] Disconnecting, connected: {}", clientId, mqttClient.isConnected());
            if (mqttClient.isConnected())
                mqttClient.disconnect();
            log.warn("[{}] Disconnected", clientId);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(final MqttMessage message) throws MqttException {
        mqttClient.publish( publishTopic, message );
    }
}
