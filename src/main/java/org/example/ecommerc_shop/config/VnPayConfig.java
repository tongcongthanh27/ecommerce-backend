package org.example.ecommerc_shop.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VnPayProperties.class)
public class VnPayConfig {
}
