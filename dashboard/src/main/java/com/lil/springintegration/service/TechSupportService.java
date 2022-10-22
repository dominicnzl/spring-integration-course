package com.lil.springintegration.service;

import com.lil.springintegration.endpoint.TechSupportMessageHandler;
import com.lil.springintegration.manage.DashboardManager;
import com.lil.springintegration.util.AppSupportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.*;
import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.MessageBuilder;


import java.util.Timer;
import java.util.TimerTask;

public class TechSupportService {

    static Logger logger = LoggerFactory.getLogger(DashboardManager.class);
    private Timer timer = new Timer();

    // TODO - refactor to use Spring Dependency Injection
    private AbstractSubscribableChannel techSupportChannel;
    private PollableChannel updateNotificationChannel;

    public TechSupportService() {
        updateNotificationChannel = (PollableChannel) DashboardManager.getDashboardContext()
                .getBean("updateNotificationQueueChannel");
        this.start();
    }

    private void start() {
        // Represents long-running process thread
        timer.schedule(new TimerTask() {
            public void run() {
                checkVersionCurrency();
            }
        }, 10000, 10000);
    }

    private void checkVersionCurrency() {
        // Check REST api for more current software version

        // For now, following results in a fake notice to the queue every 10 seconds
        updateNotificationChannel.send(MessageBuilder.withPayload("Application update required").build(), 1000L);
    }

    private static class ServiceMessageHandler extends TechSupportMessageHandler {
        protected void receiveAndAcknowledge(AppSupportStatus status) {
            TechSupportService.logger.info("Tech support service received new build notification: " + status.toString());
        }
    }
}
