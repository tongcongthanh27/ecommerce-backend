package org.example.ecommerc_shop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public record VnPayProperties(
        String url,
        String returnUrl,
        String ipnUrl,
        String tmnCode,
        String hashSecret,
        String version,
        String currency,
        String locale
) {
}
