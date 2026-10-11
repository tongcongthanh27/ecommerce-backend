package org.example.ecommerc_shop.service;

public interface EmailService {

    void sendOrderNotification(String to, String subject, String content);
}