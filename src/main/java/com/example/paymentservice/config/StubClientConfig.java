package com.example.paymentservice.config;

import com.example.paymentservice.properties.StubClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StubClientProperties.class)
public class StubClientConfig {
}