package com.alok.spring.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ContributorUserInfo implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> userDetails = new LinkedHashMap<>();
        userDetails.put("user", "Alok Singh");
        userDetails.put("userEmail", "alok.ku.singh@gmail.com");

        builder.withDetail("contributor", userDetails);
    }
}
