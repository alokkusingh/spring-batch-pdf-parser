package com.alok.spring.mqtt.utils;

import org.springframework.core.io.Resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterInputStream;

public class CertUtils {

    static public String getPemContent(Resource pemFile) throws IOException {

        byte[] pemByte = new byte[3072];

        if (pemFile == null)
            throw new RuntimeException("Pem file not found!");

        if (pemFile.getURL().getContent() instanceof InflaterInputStream)
            try(InflaterInputStream inflaterInputStream = (InflaterInputStream) pemFile.getURL().getContent()) {
                inflaterInputStream.read(pemByte);
            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException("Pem file not found!");
            }
        else
            try(BufferedInputStream bufferedInputStream = (BufferedInputStream) pemFile.getURL().getContent()) {
                bufferedInputStream.read(pemByte);
            } catch (IOException exception) {
                exception.printStackTrace();
                throw new RuntimeException("Pem file not found!");
            }

        return new String(pemByte);
    }

    public static String getClientTrustStore(String trustStoreFile) {
        URL trustStore = CertUtils.class.getClassLoader().getResource(trustStoreFile);
        return trustStore.getPath();
    }

    public static String getClientKeyStore(String keyStoreFile) {
        URL keyStore = CertUtils.class.getClassLoader().getResource(keyStoreFile);
        String encodedPath=keyStore.getPath();
        try {
            return java.net.URLDecoder.decode( encodedPath, StandardCharsets.UTF_8.name());
        }
        catch (Exception e ) {
            // likely the property value is malformed. Return it as is, hoping that
            // the requester knows what to make of it.
            return encodedPath;
        }
    }
}
